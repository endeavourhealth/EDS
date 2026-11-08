package org.endeavourhealth.queuereader;

import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.LastDataReceived;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * mini-application for bypassing the SFTP Reader and Messaging API when testing inbound transform
 * changes on developer laptops. Should not be used in any production setting.
 */
public class InsertIntoQueue {
    private static final Logger LOG = LoggerFactory.getLogger(InsertIntoQueue.class);
    private enum Publisher { EMIS, VISION, TPP }

    public static void main(String[] args) {

        try {
            ConfigManager.Initialize("queuereader");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        try {
            if (args.length == 0)
                insertIntoInboundQueueUI();
            else
                insertIntoInboundQueueCmd(args);
            System.exit(0);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    private static void insertIntoInboundQueueUI() throws Exception {

        //set the UI to black so it stands out when using Alt+Tab
        UIManager.put("ScrollPane.background", Color.BLACK);
        UIManager.put("List.background", Color.BLACK);
        UIManager.put("List.foreground", Color.WHITE);

        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.setMultiSelectionEnabled(false);
        f.setCurrentDirectory(new File("C:\\SFTPData"));
        f.setForeground(Color.PINK);
        f.setBackground(Color.PINK);

        while (true) {
            if (JFileChooser.CANCEL_OPTION == f.showOpenDialog(null)) {
                break;
            }

            File folder = f.getSelectedFile();
            if (!folder.exists()) {
                break;
            }

            String path = folder.getPath();
            Publisher publisher = null;
            String serviceIdStr = null;
            String software = null;
            String version = null;
            if (path.indexOf("emis") > -1) {
                publisher = Publisher.EMIS;
                serviceIdStr = "9d23eb25-b710-4c8b-a0ef-793b3df68c29";
                software = "EMISCSV";

                if (folder.listFiles().length > 1) {
                    version = "5.1";
                } else {
                    version = "CUSTOM";
                }
            } else if (path.indexOf("vision") > -1) {
                publisher = Publisher.VISION;
                serviceIdStr = "9e2a6b03-5645-4bea-9041-778055aa5533";
                software = "VISIONCSV";
                //version = "TEST_PACK";
                version = "0.18";
            } else if (path.indexOf("TPP") > -1) {
                publisher = Publisher.TPP;
                serviceIdStr = "3ec22b12-5b2e-4665-bc87-34072725ef21";
                software = "TPPCSV";
                version = "89";
            } else {
                throw new Exception("Unknown file type from path " + path);
            }

            serviceIdStr = JOptionPane.showInputDialog("Service ID", serviceIdStr);
            if (serviceIdStr == null) {
                break;
            }

            version = JOptionPane.showInputDialog("Version of " + software, version);
            if (version == null) {
                break;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = formatter.format(new Date());
            dateStr = JOptionPane.showInputDialog("Data Date yyyy-MM-dd HH:mm:ss", dateStr);
            if (dateStr == null) {
                break;
            }
            Date lastDataDate = null;
            if (!Strings.isNullOrEmpty(dateStr)) {
                lastDataDate = formatter.parse(dateStr);
            }

            if (!insertIntoInboundQueue(folder, serviceIdStr, publisher, software, version, lastDataDate)) {
                break;
            }
        }

    }

    private static void insertIntoInboundQueueCmd(String[] args) throws Exception {
        if (args.length < 5 || args.length > 6) {
            System.out.println("Invalid arguments, expected:-");
            System.out.println("\tfolder serviceId system software version [last data date - format = 'yyyy-MM-dd HH:mm:ss']");
            System.out.println("\n\nExample system/software/version combinations:-\n");
            System.out.println("System  \tSoftware \tVersion");
            System.out.println("~~~~~~  \t~~~~~~~~ \t~~~~~~~");
            System.out.println("emis    \tEMISCSV  \t5.1");
            System.out.println("emis    \tEMISCSV  \tCUSTOM");
            System.out.println("vision  \tVISIONCSV\t0.18");
            System.out.println("tpp     \tTPPCSV   \t89");
            System.exit(-1);
        }
        File folder = new File(args[0]);
        String serviceIdStr = args[1];
        Publisher publisher = null;
        if ("emis".equalsIgnoreCase(args[2]))
            publisher = Publisher.EMIS;
        else if ("vision".equalsIgnoreCase(args[2]))
            publisher = Publisher.VISION;
        else if ("tpp".equalsIgnoreCase(args[2]))
            publisher = Publisher.TPP;
        else
            throw new IllegalArgumentException("Unknown publisher [" + args[2] + "]");

        String software = args[3];
        String version = args[4];
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date lastDataDate = args.length == 6 ? formatter.parse(args[5]) : null;

        insertIntoInboundQueue(folder, serviceIdStr, publisher, software, version, lastDataDate);
    }

    private static boolean insertIntoInboundQueue(File folder, String serviceIdStr, Publisher publisher, String software, String version, Date lastDataDate) throws Exception {
        //create destination directory to copy to
        String sharedStoragePath = TransformConfig.instance().getSharedStoragePath();

        String dir = FilenameUtils.concat(sharedStoragePath, "CUSTOM");

        String nowStr = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(new Date());
        dir = FilenameUtils.concat(dir, nowStr);

        File dirObj = new File(dir);
        if (!dirObj.exists()) {
            dirObj.mkdirs();
        }

        String eventDesc = "Manually pushed into edsProtocol exchange from " + folder.getName();

        java.util.List<ExchangePayloadFile> body = new ArrayList<>();

        File[] files = folder.listFiles();
        for (File childFile : files) {
            if (childFile.isDirectory()) {
                throw new Exception("Unexpected directory " + childFile);
            }

            //work out file type
            String fileType = null;

            String fileName = childFile.getName();
            if (fileName.equals("desktop.ini")
                    || fileName.equalsIgnoreCase("readme.txt")) {
                continue;
            }

            if (Publisher.EMIS == publisher) {
                String[] parts = fileName.split("_");
                if (parts.length == 5) {
                    String schemaNamePart = parts[1];
                    String tableNamePart = parts[2];
                    fileType = schemaNamePart + "_" + tableNamePart;

                    //} else if (fileName.equals("EndeavourRegistrationStatusHistory.txt")) {
                } else if (fileName.contains("RegistrationStatusHistory")) {
                    fileType = "RegistrationStatus";

                    //} else if (fileName.equals("EndeavourOriginalTerm.txt")) {
                } else if (fileName.contains("OriginalTerm")) {
                    fileType = "OriginalTerms";

                } else {
                    throw new Exception("Emis batch filename could not be parsed from " + childFile);
                }
            } else if (Publisher.VISION == publisher) {
                String name = FilenameUtils.getBaseName(fileName);
                String[] toks = name.split("-");
                String first = toks[0];
                int index = first.indexOf("_");
                int second = first.indexOf("_", index + 1);
                fileType = first.substring(second + 1);
            } else if (Publisher.TPP == publisher) {
                String name = FilenameUtils.getBaseName(fileName);
                if (!name.startsWith("SR")) {
                    throw new Exception("Filename " + fileName + " doesn't start with SR");
                }
                fileType = name.substring(2);

            } else {
                throw new Exception("Unsupported publisher");
            }

            long len = childFile.length();

            //copy to shared storage path
            byte[] bytes = Files.readAllBytes(childFile.toPath());
            String dstFileStr = FilenameUtils.concat(dir, fileName);
            File dstFile = new File(dstFileStr);
            Files.write(dstFile.toPath(), bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);

            String payloadFileStr = dstFileStr.substring(sharedStoragePath.length() + 1);

            ExchangePayloadFile payload = new ExchangePayloadFile();
            payload.setType(fileType);
            payload.setSize(len);
            payload.setPath(payloadFileStr);

            body.add(payload);
        }

        if (body.isEmpty()) {
            throw new Exception("Failed to populate body from " + folder);
        }
        String bodyJson = JsonSerializer.serialize(body);

        Exchange exchange = new Exchange();
        exchange.setId(UUID.randomUUID());
        exchange.setBody(bodyJson);
        exchange.setTimestamp(new Date());
        exchange.setHeaders(new HashMap<>());

        UUID serviceId = UUID.fromString(serviceIdStr);
        exchange.setHeader(HeaderKeys.SenderServiceUuid, serviceId.toString());

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getById(serviceId);

        String odsCode = service.getLocalId();
        exchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);

        java.util.List<ServiceInterfaceEndpoint> endpoints = service.getEndpointsList();
        if (endpoints.size() != 1) {
            throw new Exception("Can't support multiple endpoints in the service");
        }
        ServiceInterfaceEndpoint endpoint = endpoints.get(0);
        UUID systemId = endpoint.getSystemUuid();
        exchange.setHeader(HeaderKeys.SenderSystemUuid, systemId.toString());

        exchange.setHeader(HeaderKeys.MessageFormat, "application/base64");
        exchange.setHeader(HeaderKeys.MessageId, UUID.randomUUID().toString());
        exchange.setHeader(HeaderKeys.ContentType, "text/xml");
        exchange.setHeader(HeaderKeys.SystemVersion, version);
        exchange.setHeader(HeaderKeys.SourceSystem, software);
        exchange.setServiceId(service.getId());
        exchange.setSystemId(systemId);

        if (lastDataDate != null) {
            exchange.setHeaderAsDate(HeaderKeys.DataDate, lastDataDate);
            exchange.setHeaderAsDate(HeaderKeys.ExtractDate, lastDataDate);
            exchange.setHeaderAsDate(HeaderKeys.ExtractCutoff, lastDataDate);
        }

        LOG.info("Exchange created");

        AuditWriter.writeExchange(exchange);
        AuditWriter.writeExchangeEvent(exchange, eventDesc);
        LOG.info("Exchange saved");

        //update "received" table
        if (lastDataDate != null) {
            LastDataReceived obj = new LastDataReceived();
            obj.setServiceId(serviceId);
            obj.setSystemId(systemId);
            obj.setExchangeId(exchange.getId());
            obj.setReceivedDate(new Date());
            obj.setExtractDate(lastDataDate);
            obj.setExtractCutoff(lastDataDate);
            LastDataDalI dal = DalProvider.factoryLastDataDal();
            dal.save(obj);
        }

        //post to InboundQueue
        java.util.List<UUID> exchangeIds = new ArrayList<>();
        exchangeIds.add(exchange.getId());
        QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.INBOUND, null, null);
        LOG.info("Exchange posted to inbound queue from " + folder.getName());

        return true;
    }
}
