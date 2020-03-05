package org.endeavourhealth.queuereader;

import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.FileInfo;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public abstract class SpecialRoutines {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialRoutines.class);

    public static void findOutOfOrderTppServices() {
        LOG.info("Finding Out of Order TPP Services");
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {
                if (service.getTags() == null
                        && !service.getTags().containsKey("TPP")) {
                    continue;
                }

                LOG.info("Checking " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");

                    //exchanges are in insert date order, most recent first
                    Date previousDate = null;

                    for (int i=0; i<exchanges.size(); i++) {
                        Exchange exchange = exchanges.get(i);

                        Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
                        if (dataDate == null) {
                            throw new Exception("No data date for exchange " + exchange.getId());
                        }

                        if (previousDate == null
                                || dataDate.before(previousDate)) {
                            previousDate = dataDate;

                        } else {
                            LOG.warn("Exchange " + exchange.getId() + " from " + exchange.getTimestamp() + " is out of order");
                        }
                    }

                }
            }

            //find TPP services
            //get exchanges
            //work from MOST recent
            //see if exchanges have data date out of order
            //how to fix?...
            //If queued up -
            //If already processed - move exchange and re-queued from AFTER bulk
            //If not processed & not queued - just move exchange?


            LOG.info("Finished Finding Out of Order TPP Services");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    public static void populateExchangeFileSizes(String odsCodeRegex) {
        LOG.info("Populating Exchange File Sizes for " + odsCodeRegex);
        try {

            String sharedStoragePath = TransformConfig.instance().getSharedStoragePath();

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {
                //check regex
                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);

                    for (Exchange exchange: exchanges) {
                        String body = exchange.getBody();
                        try {
                            String rootDir = null;
                            Map<String, ExchangePayloadFile> hmFilesByName = new HashMap<>();

                            List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body, false);
                            for (ExchangePayloadFile file: files) {
                                if (file.getSize() != null) {
                                    continue;
                                }
                                String path = file.getPath();
                                path = FilenameUtils.concat(sharedStoragePath, path);

                                String name = FilenameUtils.getName(path);
                                hmFilesByName.put(name, file);

                                String dir = new File(path).getParent();
                                if (rootDir == null
                                        || rootDir.equals(dir)) {
                                    rootDir = dir;
                                } else {
                                    throw new Exception("Files not in same directory [" + rootDir + "] vs [" + dir + "]");
                                }
                            }

                            if (!hmFilesByName.isEmpty()) {

                                List<FileInfo> s3Listing = FileHelper.listFilesInSharedStorageWithInfo(rootDir);
                                for (FileInfo s3Info : s3Listing) {
                                    String path = s3Info.getFilePath();
                                    long size = s3Info.getSize();

                                    String name = FilenameUtils.getName(path);
                                    ExchangePayloadFile file = hmFilesByName.get(name);
                                    if (file == null) {
                                        throw new Exception("No info for file " + path + " found");
                                    }

                                    file.setSize(new Long(size));
                                }

                                //write back to JSON
                                String newJson = JsonSerializer.serialize(files);
                                exchange.setBody(newJson);

                                //save to DB
                                AuditWriter.writeExchange(exchange);
                            }

                        } catch (Throwable t) {
                            throw new Exception("Failed on exchange " + exchange.getId(), t);
                        }
                    }
                }
            }

            LOG.info("Finished Populating Exchange File Sizes for " + odsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static boolean shouldSkipService(Service service, String odsCodeRegex) {
        if (odsCodeRegex == null) {
            return false;
        }

        String odsCode = service.getLocalId();
        if (Strings.isNullOrEmpty(odsCode)
                || !Pattern.matches(odsCodeRegex, odsCode)) {
            LOG.debug("Skipping " + service + " due to regex");
            return true;
        }

        return false;
    }
}
