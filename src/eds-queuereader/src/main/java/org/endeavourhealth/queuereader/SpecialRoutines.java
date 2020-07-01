package org.endeavourhealth.queuereader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.application.ApplicationHeartbeatHelper;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.DefinitionItemType;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberInstanceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataSharingAgreementCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.exceptions.TransformException;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberConfig;
import org.endeavourhealth.core.messaging.pipeline.components.DetermineRelevantProtocolIds;
import org.endeavourhealth.core.messaging.pipeline.components.MessageTransformOutbound;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.messaging.pipeline.components.RunDataDistributionProtocols;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.terminology.Read2Code;
import org.endeavourhealth.core.terminology.SnomedCode;
import org.endeavourhealth.core.terminology.TerminologyService;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.im.client.IMClient;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.common.resourceBuilders.EpisodeOfCareBuilder;
import org.endeavourhealth.transform.common.resourceBuilders.GenericBuilder;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.enterprise.EnterpriseTransformHelper;
import org.endeavourhealth.transform.enterprise.ObservationCodeHelper;
import org.endeavourhealth.transform.enterprise.transforms.OrganisationEnterpriseTransformer;
import org.endeavourhealth.transform.fhirhl7v2.FhirHl7v2Filer;
import org.endeavourhealth.transform.fhirhl7v2.transforms.EncounterTransformer;
import org.endeavourhealth.transform.subscriber.IMConstant;
import org.endeavourhealth.transform.subscriber.IMHelper;
import org.endeavourhealth.transform.subscriber.SubscriberTransformHelper;
import org.endeavourhealth.transform.subscriber.targetTables.OutputContainer;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.endeavourhealth.transform.subscriber.transforms.OrganisationTransformer;
import org.endeavourhealth.transform.subscriber.BulkHelper;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.System;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.endeavourhealth.core.xml.QueryDocument.ServiceContractType.PUBLISHER;

public abstract class SpecialRoutines {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialRoutines.class);
    private static final String COMPASS_V1 = "compass_v1";
    private static final String COMPASS_V2 = "compass_v2";



    /*public static void populateExchangeFileSizes(String odsCodeRegex) {
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

                    //skip ADT feeds because their JSON isn't the same
                    if (systemId.equals(UUID.fromString("d874c58c-91fd-41bb-993e-b1b8b22038b2"))//live
                            || systemId.equals(UUID.fromString("68096181-9e5d-4cca-821f-a9ecaa0ebc50"))) { //dev
                        continue;
                    }

                    ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");

                    int done = 0;

                    for (Exchange exchange: exchanges) {

                        boolean saveExchange = false;

                        //make sure the individual file sizes are in the JSON body
                        try {
                            String body = exchange.getBody();
                            List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body, false);
                            for (ExchangePayloadFile file: files) {
                                if (file.getSize() != null) {
                                    continue;
                                }

                                String path = file.getPath();
                                path = FilenameUtils.concat(sharedStoragePath, path);

                                String name = FilenameUtils.getName(path);
                                String dir = new File(path).getParent();

                                List<FileInfo> s3Listing = FileHelper.listFilesInSharedStorageWithInfo(dir);
                                for (FileInfo s3Info : s3Listing) {
                                    String s3Path = s3Info.getFilePath();
                                    long size = s3Info.getSize();

                                    String s3Name = FilenameUtils.getName(s3Path);
                                    if (s3Name.equals(name)) {

                                        file.setSize(new Long(size));

                                        //write back to JSON
                                        String newJson = JsonSerializer.serialize(files);
                                        exchange.setBody(newJson);

                                        saveExchange = true;
                                        break;
                                    }
                                }
                            }


                            *//*String rootDir = null;
                            Map<String, ExchangePayloadFile> hmFilesByName = new HashMap<>();

                            String body = exchange.getBody();
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
                                        LOG.debug("No info for file " + path + " found");
                                        continue;
                                        //throw new Exception();
                                    }

                                    file.setSize(new Long(size));
                                }

                                //write back to JSON
                                String newJson = JsonSerializer.serialize(files);
                                exchange.setBody(newJson);

                                saveExchange = true;
                            }*//*

                        } catch (Throwable t) {
                            throw new Exception("Failed on exchange " + exchange.getId(), t);
                        }

                        //and make sure the total size is in the headers
                        Long totalSize = exchange.getHeaderAsLong(HeaderKeys.TotalFileSize);
                        if (totalSize == null) {

                            long size = 0;

                            String body = exchange.getBody();
                            List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body, false);
                            for (ExchangePayloadFile file: files) {
                                if (file.getSize() == null) {
                                    throw new Exception("No file size for " + file.getPath() + " in exchange " + exchange.getId());
                                }

                                size += file.getSize().longValue();
                            }

                            exchange.setHeaderAsLong(HeaderKeys.TotalFileSize, new Long(size));
                            saveExchange = true;
                        }

                        //save to DB
                        if (saveExchange) {
                            AuditWriter.writeExchange(exchange);
                        }

                        done ++;
                        if (done % 100 == 0) {
                            LOG.debug("Done " + done);
                        }
                    }

                    LOG.debug("Finished at " + done);
                }
            }

            LOG.info("Finished Populating Exchange File Sizes for " + odsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    public static boolean shouldSkipService(Service service, String odsCodeRegex) {
        if (Strings.isNullOrEmpty(odsCodeRegex)) {
            return false;
        }

        String odsCode = service.getLocalId();
        if (!Strings.isNullOrEmpty(odsCode)
                && Pattern.matches(odsCodeRegex, odsCode)) {
            return false;
        }

        String ccgCode = service.getCcgCode();
        if (!Strings.isNullOrEmpty(ccgCode)
                && Pattern.matches(odsCodeRegex, ccgCode)) {
            return false;
        }

        LOG.debug("Skipping " + service + " due to regex");
        return true;
    }


    public static void getResourceHistory(String serviceIdStr, String resourceTypeStr, String resourceIdStr) {
        LOG.debug("Getting resource history for " + resourceTypeStr + " " + resourceIdStr + " for service " + serviceIdStr);
        try {
            LOG.debug("");
            LOG.debug("");

            UUID serviceId = UUID.fromString(serviceIdStr);
            UUID resourceId = UUID.fromString(resourceIdStr);

            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);

            ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            ResourceWrapper retrieved = resourceDal.getCurrentVersion(serviceId, resourceType.toString(), resourceId);
            LOG.debug("Retrieved current>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LOG.debug("");
            LOG.debug("" + retrieved);
            LOG.debug("");
            LOG.debug("");

            List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), resourceId);
            LOG.debug("Retrieved history " + history.size() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LOG.debug("");
            for (ResourceWrapper h: history) {
                LOG.debug("" + h);
                LOG.debug("");
            }


        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * validates NHS numbers from a file, outputting valid and invalid ones to separate output files
     * if the addComma parameter is true it'll add a comma to the end of each line, so it's ready
     * for sending to the National Data Opt-out service
     */
    public static void validateNhsNumbers(String filePath, boolean addCommas) {
        LOG.info("Validating NHS Numbers in " + filePath);
        LOG.info("Adding commas = " + addCommas);
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                throw new Exception("File " + f + " doesn't exist");
            }
            List<String> lines = FileUtils.readLines(f);

            List<String> valid = new ArrayList<>();
            List<String> invalid = new ArrayList<>();

            for (String line: lines) {

                if (line.length() > 10) {
                    String c = line.substring(10);
                    if (c.equals(",")) {
                        line = line.substring(0, 10);
                    } else {
                        invalid.add(line);
                        continue;
                    }
                }

                if (line.length() < 10) {
                    invalid.add(line);
                    continue;
                }

                Boolean isValid = IdentifierHelper.isValidNhsNumber(line);
                if (isValid == null) {
                    continue;
                }

                if (!isValid.booleanValue()) {
                    invalid.add(line);
                    continue;
                }

                //if we make it here, we're valid
                if (addCommas) {
                    line += ",";
                }
                valid.add(line);
            }

            File dir = f.getParentFile();
            String fileName = f.getName();

            File fValid = new File(dir, "VALID_" + fileName);
            FileUtils.writeLines(fValid, valid);
            LOG.info("" + valid.size() + " NHS numbers written to " + fValid);

            File fInvalid = new File(dir, "INVALID_" + fileName);
            FileUtils.writeLines(fInvalid, invalid);
            LOG.info("" + invalid.size() + " NHS numbers written to " + fInvalid);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    public static void getJarDetails() {
        LOG.debug("Get Jar Details OLD");
        try {
            Class cls = SpecialRoutines.class;
            LOG.debug("Cls = " + cls);
            ProtectionDomain domain = cls.getProtectionDomain();
            LOG.debug("Domain = " + domain);
            CodeSource source = domain.getCodeSource();
            LOG.debug("Source = " + source);
            URL loc = source.getLocation();
            LOG.debug("Location = " + loc);
            URI uri = loc.toURI();
            LOG.debug("URI = " + uri);
            File f = new File(uri);
            LOG.debug("File = " + f);

            Date d = new Date(f.lastModified());
            LOG.debug("Last Modified = " + d);

        } catch (Throwable t) {
            LOG.error("", t);
        }

        LOG.debug("");
        LOG.debug("");
        LOG.debug("Get Jar Details THIS class");
        try {
            Class cls = SpecialRoutines.class;
            ApplicationHeartbeatHelper.findJarDateTime(cls, true);

        } catch (Throwable t) {
            LOG.error("", t);
        }

        LOG.debug("");
        LOG.debug("");
        LOG.debug("Get Jar Details CORE class");
        try {
            Class cls = ApplicationHeartbeatHelper.class;
            ApplicationHeartbeatHelper.findJarDateTime(cls, true);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void breakUpAdminBatches(String odsCodeRegex) {
        try {
            LOG.debug("Breaking up admin batches for " + odsCodeRegex);

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {
                //check regex
                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                String publisherConfig = service.getPublisherConfigName();
                if (Strings.isNullOrEmpty(publisherConfig)) {
                    continue;
                }

                Connection ehrConnection = ConnectionManager.getEhrNonPooledConnection(service.getId());
                Connection auditConnection = ConnectionManager.getAuditNonPooledConnection();
                int maxSize = TransformConfig.instance().getAdminBatchMaxSize();

                LOG.debug("Doing " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                for (UUID systemId: systemIds) {

                    ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                    ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();

                    List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");

                    int done = 0;
                    int fixed = 0;

                    for (Exchange exchange: exchanges) {
                        List<ExchangeBatch> batches = exchangeBatchDal.retrieveForExchangeId(exchange.getId());
                        for (ExchangeBatch batch: batches) {
                            if (batch.getEdsPatientId() != null) {
                                continue;
                            }

                            String sql = "SELECT COUNT(1) FROM resource_history WHERE exchange_batch_id = ?";

                            PreparedStatement psSelectCount = ehrConnection.prepareStatement(sql);
                            psSelectCount.setString(1, batch.getBatchId().toString());
                            ResultSet rs = psSelectCount.executeQuery();
                            rs.next();
                            int count = rs.getInt(1);
                            psSelectCount.close();

                            if (count > maxSize) {
                                LOG.debug("Fixing batch " + batch.getBatchId() + " for exchange " + exchange.getId() + " with " + count + " resources");

                                //work backwards
                                int numBlocks = count / maxSize;
                                if (count % maxSize > 0) {
                                    numBlocks ++;
                                }

                                //exchange_event - to audit this
                                PreparedStatement psExchangeEvent = null;

                                sql = "INSERT INTO exchange_event"
                                        + " VALUES (?, ?, ?, ?)";
                                psExchangeEvent = auditConnection.prepareStatement(sql);
                                psExchangeEvent.setString(1, UUID.randomUUID().toString());
                                psExchangeEvent.setString(2, exchange.getId().toString());
                                psExchangeEvent.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
                                psExchangeEvent.setString(4, "Split large admin batch of " + count + " resources into blocks of " + maxSize);
                                psExchangeEvent.executeUpdate();

                                auditConnection.commit();
                                psExchangeEvent.close();

                                for (int blockNum=numBlocks; blockNum>1; blockNum--) { //skip block one as we want to leave it alone

                                    UUID newId = UUID.randomUUID();
                                    int rangeStart = (blockNum-1) * maxSize;

                                    LOG.debug("Updating " + batch.getBatchId() + " " + rangeStart + " to " + (rangeStart+maxSize) + " to batch ID " + newId);

                                    PreparedStatement psExchangeBatch = null;
                                    PreparedStatement psSendAudit = null;
                                    PreparedStatement psTransformAudit = null;

                                    //insert into exchange_batch table
                                    sql = "INSERT INTO exchange_batch"
                                            + " SELECT exchange_id, '" + newId.toString() + "', inserted_at, eds_patient_id"
                                            + " FROM exchange_batch"
                                            + " WHERE exchange_id = ? AND batch_id = ?";
                                    psExchangeBatch = auditConnection.prepareStatement(sql);
                                    psExchangeBatch.setString(1, exchange.getId().toString());
                                    psExchangeBatch.setString(2, batch.getBatchId().toString());
                                    psExchangeBatch.executeUpdate();

                                    //exchange_subscriber_send_audit
                                    sql = "INSERT INTO exchange_subscriber_send_audit"
                                            + " SELECT exchange_id, '" + newId.toString() + "', subscriber_config_name, inserted_at, error_xml, queued_message_id"
                                            + " FROM exchange_subscriber_send_audit"
                                            + " WHERE exchange_id = ? AND exchange_batch_id = ?";
                                    psSendAudit = auditConnection.prepareStatement(sql);
                                    psSendAudit.setString(1, exchange.getId().toString());
                                    psSendAudit.setString(2, batch.getBatchId().toString());
                                    psSendAudit.executeUpdate();

                                    //exchange_subscriber_transform_audit
                                    sql = "INSERT INTO exchange_subscriber_transform_audit"
                                            + " SELECT exchange_id, '" + newId.toString() + "', subscriber_config_name, started, ended, error_xml, number_resources_transformed, queued_message_id"
                                            + " FROM exchange_subscriber_transform_audit"
                                            + " WHERE exchange_id = ? AND exchange_batch_id = ?";
                                    psTransformAudit = auditConnection.prepareStatement(sql);
                                    psTransformAudit.setString(1, exchange.getId().toString());
                                    psTransformAudit.setString(2, batch.getBatchId().toString());
                                    psTransformAudit.executeUpdate();

                                    auditConnection.commit();
                                    psExchangeBatch.close();
                                    psSendAudit.close();
                                    psTransformAudit.close();

                                    //update history
                                    PreparedStatement psDropTempTable = null;
                                    PreparedStatement psCreateTempTable = null;
                                    PreparedStatement psIndexTempTable = null;
                                    PreparedStatement psUpdateHistory = null;

                                    sql = "DROP TEMPORARY TABLE IF EXISTS tmp.tmp_admin_batch_fix";
                                    psDropTempTable = ehrConnection.prepareStatement(sql);
                                    psDropTempTable.executeUpdate();

                                    sql = "CREATE TEMPORARY TABLE tmp.tmp_admin_batch_fix AS"
                                            + " SELECT resource_id, resource_type, created_at, version"
                                            + " FROM resource_history"
                                            + " WHERE exchange_batch_id = ?"
                                            + " ORDER BY resource_type, resource_id"
                                            + " LIMIT " + rangeStart + ", " + maxSize;
                                    psCreateTempTable = ehrConnection.prepareStatement(sql);
                                    psCreateTempTable.setString(1, batch.getBatchId().toString());
                                    psCreateTempTable.executeUpdate();

                                    sql = "CREATE INDEX ix ON tmp.tmp_admin_batch_fix (resource_id, resource_type, created_at, version)";
                                    psIndexTempTable = ehrConnection.prepareStatement(sql);
                                    psIndexTempTable.executeUpdate();

                                    sql = "UPDATE resource_history"
                                            + " INNER JOIN tmp.tmp_admin_batch_fix f"
                                            + " ON f.resource_id = resource_history.resource_id"
                                            + " AND f.resource_type = f.resource_type"
                                            + " AND f.created_at = f.created_at"
                                            + " AND f.version = f.version"
                                            + " SET resource_history.created_at = resource_history.created_at,"
                                            + " resource_history.exchange_batch_id = ?";
                                    psUpdateHistory = ehrConnection.prepareStatement(sql);
                                    psUpdateHistory.setString(1, newId.toString());
                                    psUpdateHistory.executeUpdate();

                                    ehrConnection.commit();
                                    psDropTempTable.close();
                                    psCreateTempTable.close();
                                    psIndexTempTable.close();
                                    psUpdateHistory.close();
                                }
                            }
                        }

                        done ++;
                        if (done % 100 == 0) {
                            LOG.debug("Checked " + done + " exchanges and fixed " + fixed);
                        }
                    }

                    LOG.debug("Checked " + done + " exchanges and fixed " + fixed);
                }

                ehrConnection.close();
                auditConnection.close();

            }

            LOG.debug("Finished breaking up admin batches for " + odsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void testInformationModel() {
        LOG.debug("Testing Information Model");
        try {
            LOG.debug("----getMappedCoreCodeForSchemeCode------------------------------------------------");
            LOG.debug("    gets Snomed concept ID for legacy code and scheme");

            String legacyCode = "C10..";
            String legacyScheme = IMConstant.READ2;
            String mappedCoreCode = IMClient.getMappedCoreCodeForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got Snomed Concept ID " + mappedCoreCode);

            legacyCode =  "C10..";
            legacyScheme = IMConstant.CTV3;
            mappedCoreCode = IMClient.getMappedCoreCodeForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got Snomed Concept ID " + mappedCoreCode);

            legacyCode = "G33..";
            legacyScheme = IMConstant.READ2;
            mappedCoreCode = IMClient.getMappedCoreCodeForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got Snomed Concept ID " + mappedCoreCode);

            legacyCode = "G33..";
            legacyScheme = IMConstant.CTV3;
            mappedCoreCode = IMClient.getMappedCoreCodeForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got Snomed Concept ID " + mappedCoreCode);

            legacyCode = "687309281";
            legacyScheme = IMConstant.BARTS_CERNER;
            mappedCoreCode = IMClient.getMappedCoreCodeForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got Snomed Concept ID " + mappedCoreCode);


            LOG.debug("----getConceptDbidForSchemeCode------------------------------------------------");
            LOG.debug("    gets NON-CORE DBID for legacy code and scheme");

            legacyCode = "C10..";
            legacyScheme = IMConstant.READ2;
            Integer dbid = IMClient.getConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got non-core DBID " + dbid);

            legacyCode = "C10..";
            legacyScheme = IMConstant.CTV3;
            dbid = IMClient.getConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got non-core DBID " + dbid);

            legacyCode = "G33..";
            legacyScheme = IMConstant.READ2;
            dbid = IMClient.getConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got non-core DBID " + dbid);

            legacyCode = "G33..";
            legacyScheme = IMConstant.CTV3;
            dbid = IMClient.getConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got non-core DBID " + dbid);

            legacyCode = "687309281";
            legacyScheme = IMConstant.BARTS_CERNER;
            dbid = IMClient.getConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got non-core DBID " + dbid);


            LOG.debug("----getMappedCoreConceptDbidForSchemeCode------------------------------------------------");
            LOG.debug("    gets CORE DBID for legacy code and scheme");

            legacyCode = "C10..";
            legacyScheme = IMConstant.READ2;
            dbid = IMClient.getMappedCoreConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got core DBID " + dbid);

            legacyCode = "C10..";
            legacyScheme = IMConstant.CTV3;
            dbid = IMClient.getMappedCoreConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got core DBID " + dbid);

            legacyCode = "G33..";
            legacyScheme = IMConstant.READ2;
            dbid = IMClient.getMappedCoreConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got core DBID " + dbid);

            legacyCode = "G33..";
            legacyScheme = IMConstant.CTV3;
            dbid = IMClient.getMappedCoreConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got core DBID " + dbid);

            legacyCode = "687309281";
            legacyScheme = IMConstant.BARTS_CERNER;
            dbid = IMClient.getMappedCoreConceptDbidForSchemeCode(legacyScheme, legacyCode);
            LOG.debug("For " + legacyScheme + " " + legacyCode + ", got core DBID " + dbid);


            LOG.debug("----getCodeForConceptDbid------------------------------------------------");
            LOG.debug("    get Snomed concept ID for CORE DBID");

            Integer coreConceptDbId = new Integer(61367);
            String codeForConcept = IMClient.getCodeForConceptDbid(coreConceptDbId);
            LOG.debug("For core DBID " + coreConceptDbId + " got " + codeForConcept);

            coreConceptDbId = new Integer(123390);
            codeForConcept = IMClient.getCodeForConceptDbid(coreConceptDbId);
            LOG.debug("For core DBID " + coreConceptDbId + " got " + codeForConcept);

            coreConceptDbId = new Integer(1406482);
            codeForConcept = IMClient.getCodeForConceptDbid(coreConceptDbId);
            LOG.debug("For core DBID " + coreConceptDbId + " got " + codeForConcept);

            LOG.debug("----getConceptDbidForTypeTerm------------------------------------------------");
            LOG.debug("    gets NON-CORE DBID for encounter type text");

            String encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            String encounterTerm = "Clinical";
            Integer encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            encounterTerm = "Administrative";
            encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            encounterTerm = "GP Surgery";
            encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);


            LOG.debug("----getMappedCoreConceptDbidForTypeTerm------------------------------------------------");
            LOG.debug("    gets CORE DBID for encounter type text");

            encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            encounterTerm = "Clinical";
            encounterConceptId = IMClient.getMappedCoreConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            encounterTerm = "Administrative";
            encounterConceptId = IMClient.getMappedCoreConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.ENCOUNTER_LEGACY;
            encounterTerm = "GP Surgery";
            encounterConceptId = IMClient.getMappedCoreConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            LOG.debug("----getCodeForTypeTerm------------------------------------------------");
            LOG.debug("    gets locally generated Cerner code for test code and result text");

            String code = "687309281";
            String scheme = IMConstant.BARTS_CERNER;
            String term = "SARS-CoV-2 RNA DETECTED";
            String codeForTypeTerm = IMClient.getCodeForTypeTerm(scheme, code, term);
            LOG.debug("For " + scheme + " " + code + " [" + term + "] got " + codeForTypeTerm);

            code = "687309281";
            scheme = IMConstant.BARTS_CERNER;
            term = "SARS-CoV-2 RNA NOT detected";
            codeForTypeTerm = IMClient.getCodeForTypeTerm(scheme, code, term);
            LOG.debug("For " + scheme + " " + code + " [" + term + "] got " + codeForTypeTerm);

            LOG.debug("");
            LOG.debug("");
            LOG.debug("----Coronavirus test IMCLIENT------------------------------------------------");

            String testCode = "687309281";
            String positiveResult = "SARS-CoV-2 RNA DETECTED";
            String negativeResult = "SARS-CoV-2 RNA NOT detected";

            LOG.debug("Want to find Snomed concept 1240511000000106 from Cerner test code 687309281");
            String testCodeSnomedConceptId = IMClient.getMappedCoreCodeForSchemeCode(IMConstant.BARTS_CERNER, testCode);
            LOG.debug("Got Snomed test code " + testCodeSnomedConceptId);
            LOG.debug("");

            LOG.debug("Want to get locally generated Cerner code for test code and positive textual result");
            String locallyGeneratedPositiveCode = IMClient.getCodeForTypeTerm(IMConstant.BARTS_CERNER, testCode, positiveResult);
            LOG.debug("Got locally generated code " + locallyGeneratedPositiveCode);
            LOG.debug("");

            LOG.debug("Want to get locally generated Cerner code for test code and negative textual result");
            String locallyGeneratedNegativeCode = IMClient.getCodeForTypeTerm(IMConstant.BARTS_CERNER, testCode, negativeResult);
            LOG.debug("Got locally generated code " + locallyGeneratedNegativeCode);
            LOG.debug("");

            LOG.debug("Want to get Snomed concept ID for test code and positive result");
            String snomedPositiveCode = IMClient.getMappedCoreCodeForSchemeCode(IMConstant.BARTS_CERNER, locallyGeneratedPositiveCode);
            LOG.debug("Got positive snomed code " + snomedPositiveCode);
            LOG.debug("");

            LOG.debug("Want to get Snomed concept ID for test code and negative result");
            String snomedNegativeCode = IMClient.getMappedCoreCodeForSchemeCode(IMConstant.BARTS_CERNER, locallyGeneratedNegativeCode);
            LOG.debug("Got negative snomed code " + snomedNegativeCode);
            LOG.debug("");


            LOG.debug("");
            LOG.debug("");
            LOG.debug("----Coronavirus test IMHELPER------------------------------------------------");

            LOG.debug("Want to find Snomed concept 1240511000000106 from Cerner test code 687309281");
            testCodeSnomedConceptId = IMHelper.getMappedSnomedConceptForSchemeCode(IMConstant.BARTS_CERNER, testCode);
            LOG.debug("Got Snomed test code " + testCodeSnomedConceptId);
            LOG.debug("");

            LOG.debug("Want to get locally generated Cerner code for test code and positive textual result");
            locallyGeneratedPositiveCode = IMHelper.getMappedLegacyCodeForLegacyCodeAndTerm(IMConstant.BARTS_CERNER, testCode, positiveResult);
            LOG.debug("Got locally generated code " + locallyGeneratedPositiveCode);
            LOG.debug("");

            LOG.debug("Want to get locally generated Cerner code for test code and negative textual result");
            locallyGeneratedNegativeCode = IMHelper.getMappedLegacyCodeForLegacyCodeAndTerm(IMConstant.BARTS_CERNER, testCode, negativeResult);
            LOG.debug("Got locally generated code " + locallyGeneratedNegativeCode);
            LOG.debug("");

            LOG.debug("Want to get Snomed concept ID for test code and positive result");
            snomedPositiveCode = IMHelper.getMappedSnomedConceptForSchemeCode(IMConstant.BARTS_CERNER, locallyGeneratedPositiveCode);
            LOG.debug("Got positive snomed code " + snomedPositiveCode);
            LOG.debug("");

            LOG.debug("Want to get Snomed concept ID for test code and negative result");
            snomedNegativeCode = IMHelper.getMappedSnomedConceptForSchemeCode(IMConstant.BARTS_CERNER, locallyGeneratedNegativeCode);
            LOG.debug("Got negative snomed code " + snomedNegativeCode);
            LOG.debug("");

            LOG.debug("");
            LOG.debug("");
            LOG.debug("----ENCOUNTER TERM test IMHELPER------------------------------------------------");

            List<String> encounterTerms = new ArrayList<>();

            //hospital terms
            encounterTerms.add("Outpatient Register a patient");
            encounterTerms.add("Outpatient Discharge/end visit");
            encounterTerms.add("Outpatient Update patient information");
            encounterTerms.add("Emergency department Register a patient (emergency)");
            encounterTerms.add("Inpatient Transfer a patient");
            encounterTerms.add("Emergency department Discharge/end visit (emergency)");
            encounterTerms.add("Outpatient Referral Update patient information (waitinglist)");
            encounterTerms.add("Outpatient Referral Pre-admit a patient (waitinglist)");
            encounterTerms.add("Inpatient Discharge/end visit");
            encounterTerms.add("Inpatient Admit/visit notification");
            encounterTerms.add("Emergency department Update patient information (emergency)");
            encounterTerms.add("Outpatient Change an inpatient to an outpatient");

            //primary care terms
            encounterTerms.add("Telephone call to a patient");
            encounterTerms.add("G.P.Surgery");
            encounterTerms.add("Externally entered");
            encounterTerms.add("Third party");
            encounterTerms.add("GP Surgery");
            encounterTerms.add("Scanned document");
            encounterTerms.add("Docman");
            encounterTerms.add("D.N.A.");
            encounterTerms.add("Telephone");
            encounterTerms.add("Message");
            encounterTerms.add("Path. Lab.");
            encounterTerms.add("Administration note");
            encounterTerms.add("Telephone consultation");
            encounterTerms.add("Main Surgery");

            for (String encTerm: encounterTerms) {

                LOG.debug("" + encTerm + " -> ");

                String legacyEncCode = IMHelper.getMappedLegacyCodeForLegacyCodeAndTerm(IMConstant.ENCOUNTER_LEGACY, "TYPE", encTerm);
                LOG.debug("        legacy code = " + legacyEncCode);

                /*String snomedCode = null;
                if (!Strings.isNullOrEmpty(legacyEncCode)) {
                    snomedCode = IMHelper.getMappedSnomedConceptForSchemeCode(IMConstant.ENCOUNTER_LEGACY, legacyEncCode);
                }
                LOG.debug("" + encTerm + " -> " + (legacyEncCode != null ? legacyEncCode : "NULL") + " -> " + (snomedCode != null ? snomedCode : "NULL"));*/

                Integer legacyDbId = IMHelper.getConceptDbidForTypeTerm(null, IMConstant.ENCOUNTER_LEGACY, encTerm);
                Integer coreDbId = IMHelper.getIMMappedConceptForTypeTerm(null, IMConstant.ENCOUNTER_LEGACY, encTerm);
                LOG.debug("        legacy DB ID = " + legacyDbId + ", core DB ID = " + coreDbId);

//                Integer oldLegacyDbId = IMHelper.getConceptDbidForTypeTerm(null, IMConstant.DCE_Type_of_encounter, encTerm);
//                Integer oldCoreDbId = IMHelper.getIMMappedConceptForTypeTerm(null, null, IMConstant.DCE_Type_of_encounter, encTerm);
//                LOG.debug("        OLD legacy DB ID = " + oldLegacyDbId + ", OLD core DB ID = " + oldCoreDbId);



            }



            LOG.debug("Finished Testing Information Model");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void testDsm(String odsCode) {
        LOG.info("Testing DSM for " + odsCode);
        try {

            LOG.debug("Testing doesOrganisationHaveDPA");
            boolean b = OrganisationCache.doesOrganisationHaveDPA(odsCode);
            LOG.debug("Got " + b);
            LOG.debug("");
            LOG.debug("");

            LOG.debug("Testing getAllDSAsForPublisherOrg");
            List<DataSharingAgreementEntity> list = DataSharingAgreementCache.getAllDSAsForPublisherOrg(odsCode);
            if (list == null) {
                LOG.debug("Got NULL");
            } else {
                LOG.debug("Got " + list.size());
                for (DataSharingAgreementEntity e: list) {
                    LOG.debug(" -> " + e.getName() + " " + e.getUuid());
                }
            }
            LOG.debug("");
            LOG.debug("");

            LOG.debug("Testing getAllProjectsForSubscriberOrg");
            List<ProjectEntity> projects = ProjectCache.getAllProjectsForSubscriberOrg(odsCode);
            Set<String> projectIds = new HashSet<>();
            if (list == null) {
                LOG.debug("Got NULL");
            } else {
                LOG.debug("Got " + list.size());
                for (ProjectEntity project: projects) {
                    LOG.debug(" -> " + project.getName() + " " + project.getUuid());

                    String projectUuid = project.getUuid();
                    projectIds.add(projectUuid);
                }
            }
            LOG.debug("");
            LOG.debug("");

            LOG.debug("Testing getAllPublishersForProjectWithSubscriberCheck");
            if (projectIds.isEmpty()) {
                LOG.debug(" -> no project IDs");
            } else {
                for (String projectId: projectIds) {
                    LOG.debug("PROJECT ID " + projectId);
                    List<String> results = ProjectCache.getAllPublishersForProjectWithSubscriberCheck(projectId, odsCode);
                    LOG.debug("Got publisher ODS codes: " + results);
                }
            }

            LOG.debug("Testing getValidDistributionProjectsForPublisher");
            List<ProjectEntity> validDistributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
            if (validDistributionProjects.size() < 1) {
                LOG.debug("Got no valid projects");
            } else {
                LOG.debug("Got " + validDistributionProjects.size() + " valid projects");
                for (ProjectEntity project: validDistributionProjects) {
                    LOG.debug(" -> " + project.getName() + " " + project.getUuid());
                }
            }
            LOG.debug("");
            LOG.debug("");

            LOG.debug("");
            LOG.debug("");


            LOG.info("Finished Testing DSM for " + odsCode);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void compareDsm(boolean logDifferencesOnly, String toFile, String odsCodeRegex) {
        LOG.debug("Comparing DSM to DDS-UI for " + odsCodeRegex);
        LOG.debug("logDifferencesOnly = " + logDifferencesOnly);
        LOG.debug("toFile = " + toFile);
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            File dstFile = new File(toFile);
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                    .withHeader("Name", "ODS Code", "Parent Code", "Notes", "DDS-UI DPA", "DSM DPA", "DPA matches", "DDS-UI Endpoints", "DSM Endpoints", "DSA matches"
                    );
            CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                String odsCode = service.getLocalId();
                UUID serviceId = service.getId();

                //skip if filtering on ODS code
                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                //only do if a publisher
                String publisherConfig = service.getPublisherConfigName();
                if (Strings.isNullOrEmpty(publisherConfig)) {
                    continue;
                }

                List<String> logging = new ArrayList<>();
                boolean gotDifference = false;

                logging.add("Doing " + service + "--------------------------------------------------------------------");

                //check DPAs

                //check if publisher to DDS protocol
                List<String> protocolIdsOldWay = DetermineRelevantProtocolIds.getProtocolIdsForPublisherServiceOldWay(serviceId.toString());
                boolean hasDpaOldWay = !protocolIdsOldWay.isEmpty(); //in the old way, we count as having a DPA if they're in any protocol

                //check if DSM DPA
                boolean hasDpaNewWay = OrganisationCache.doesOrganisationHaveDPA(odsCode);

                boolean dpaMatches = hasDpaNewWay == hasDpaOldWay;

                if (!dpaMatches) {
                    logging.add("Old and new DPA do not match (old = " + hasDpaOldWay + ", new = " + hasDpaNewWay + ")");
                    gotDifference = true;

                } else {

                    logging.add("Old and new DPA match (" + hasDpaOldWay + ")");
                }

                //check DSAs

                //want to find target subscriber config names OLD way
                Set<String> subscriberConfigNamesOldWay = new HashSet<>();

                for (String oldProtocolId: protocolIdsOldWay) {
                    LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(UUID.fromString(oldProtocolId));
                    Protocol protocol = libraryItem.getProtocol();

                    //skip disabled protocols
                    if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
                        continue;
                    }

                    List<ServiceContract> subscribers = protocol
                            .getServiceContract()
                            .stream()
                            .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                            .filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
                            .collect(Collectors.toList());

                    for (ServiceContract serviceContract : subscribers) {
                        String subscriberConfigName = MessageTransformOutbound.getSubscriberEndpoint(serviceContract);
                        if (!Strings.isNullOrEmpty(subscriberConfigName)) {
                            subscriberConfigNamesOldWay.add(subscriberConfigName);
                        }
                    }
                }

                //find target subscriber config names NEW way
                Set<String> subscriberConfigNamesNewWay = new HashSet<>();

                List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
                if (distributionProjects == null) {
                    logging.add("Got NULL distribution projects for " + odsCode);

                } else {
                    for (ProjectEntity distributionProject: distributionProjects) {
                        String configName = distributionProject.getConfigName();
                        if (!Strings.isNullOrEmpty(configName)) {
                            subscriberConfigNamesNewWay.add(configName);
                        }
                    }
                }

                //compare the two
                List<String> l = new ArrayList<>(subscriberConfigNamesOldWay);
                l.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));
                String subscribersOldWay = String.join(", ", l);

                l = new ArrayList<>(subscriberConfigNamesNewWay);
                l.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));
                String subscribersNewWay = String.join(", ", l);

                boolean dsaMatches = subscribersOldWay.equals(subscribersNewWay);


                //flatten the tags to a String
                String notesStr = "";
                if (service.getTags() != null) {
                    List<String> toks = new ArrayList<>();

                    Map<String, String> tags = service.getTags();
                    List<String> keys = new ArrayList<>(tags.keySet());
                    keys.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));

                    for (String key: keys) {

                        String s = key;
                        String val = tags.get(key);
                        if (val != null) {
                            s += " " + val;
                        }
                        toks.add(s);
                    }

                    notesStr = String.join(", ", toks);
                }

                printer.printRecord(service.getName(), odsCode, service.getCcgCode(), notesStr, hasDpaOldWay, hasDpaNewWay, dpaMatches, subscribersOldWay, subscribersNewWay, dsaMatches);

                logging.add("");

                //log what we found if we need to
                if (!logDifferencesOnly || gotDifference) {
                    for (String line: logging) {
                        LOG.debug(line);
                    }
                }
            }

            printer.close();

            LOG.debug("Finished Comparing DSM to DDS-UI for " + odsCodeRegex + " to " + toFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * generates the JSON for the config record to move config from DDS-UI protocols to DSM (+JSON)
     */
    public static void createConfigJsonForDSM(String ddsUiProtocolName, String dsmDsaId) {
        LOG.debug("Creating Config JSON for DDS-UI -> DSM migration");
        try {
            //get DDS-UI protocol details
            LibraryItem libraryItem = BulkHelper.findProtocolLibraryItem(ddsUiProtocolName);
            LOG.debug("DDS-UI protocol [" + ddsUiProtocolName + "]");
            LOG.debug("DDS-UI protocol UUID " + libraryItem.getUuid());

            //get DSM DPA details
            DataSharingAgreementEntity dsa = DataSharingAgreementCache.getDSADetails(dsmDsaId);
            LOG.debug("DSM DSA [" + dsa.getName() + "]");
            LOG.debug("DSM DSA UUID " + dsa.getUuid());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = new ObjectNode(mapper.getNodeFactory());

            //put in DSA name for visibility
            root.put("dsa_name", dsa.getName());

            //subscriber config
            ArrayNode subscriberArray = root.putArray("subscribers");

            Protocol protocol = libraryItem.getProtocol();
            if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
                throw new Exception("Protocol isn't enabled");
            }

            List<ServiceContract> subscribers = protocol
                    .getServiceContract()
                    .stream()
                    .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                    .filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
                    .collect(Collectors.toList());

            for (ServiceContract serviceContract: subscribers) {
                String subscriberConfigName = MessageTransformOutbound.getSubscriberEndpoint(serviceContract);
                subscriberArray.add(subscriberConfigName);
            }

            //cohort stuff
            ObjectNode cohortRoot = root.putObject("cohort");

            String cohort = protocol.getCohort();
            if (cohort.equals("All Patients")) {
                cohortRoot.put("type", "all_patients");

            } else if (cohort.equals("Explicit Patients")) {
                //database is dependent on protocol ID, but I don't think this used
                throw new Exception("Protocol uses explicit patient cohort so cannot be converted");
                //cohortRoot.put("type", "explicit_patients");

            } else if (cohort.startsWith("Defining Services")) {
                cohortRoot.put("type", "registered_at");

                ArrayNode registeredAtRoot = cohortRoot.putArray("services");

                int index = cohort.indexOf(":");
                if (index == -1) {
                    throw new RuntimeException("Invalid cohort format " + cohort);
                }
                String suffix = cohort.substring(index+1);
                String[] toks = suffix.split("\r|\n|,| |;");
                for (String tok: toks) {
                    String odsCode = tok.trim().toUpperCase();  //when checking, we always make uppercase
                    if (!Strings.isNullOrEmpty(tok)) {
                        registeredAtRoot.add(odsCode);
                    }
                }

            } else {
                throw new PipelineException("Unknown cohort [" + cohort + "]");
            }

            String json = mapper.writeValueAsString(root);
            LOG.debug("JSON:\r\n\r\n" + json + "\r\n\r\n");

            LOG.debug("Finished Creating Config JSON for DDS-UI -> DSM migration");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void testBulkLoad(String s3Path, String tableName) {
        LOG.debug("Testing Bulk Load from " + s3Path + " to " + tableName);
        try {

            File dst = FileHelper.copyFileFromStorageToTempDirIfNecessary(s3Path);
            LOG.debug("Tmp file = " + dst);

            LOG.debug("Dst exists = " + dst.exists());
            LOG.debug("Dst len = " + dst.length());

            //bulk load
            Connection connection = ConnectionManager.getEdsConnection();
            connection.setAutoCommit(true);

            String path = dst.getAbsolutePath();
            path = path.replace("\\", "\\\\");

            String sql = "LOAD DATA LOCAL INFILE '" + path + "'"
                    + " INTO TABLE " + tableName
                    + " FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"'"
                    + " LINES TERMINATED BY '\\r\\n'"
                    + " IGNORE 1 LINES";
            LOG.debug("Load SQL = " + sql);

            LOG.debug("Starting bulk load");
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            //connection.commit();
            LOG.debug("Finished bulk load");

            statement.close();

            connection.setAutoCommit(false);
            connection.close();

            LOG.debug("Deleting temp file " + dst);
            FileHelper.deleteFileFromTempDirIfNecessary(dst);
            LOG.debug("Dst exists = " + dst.exists());

            LOG.debug("Finished Testing Bulk Load from " + s3Path + " to " + tableName);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /*public static void testCallToDdsUi() {

        try {
            //get the Keycloak details from the EMIS config
            JsonNode json = ConfigManager.getConfigurationAsJson("emis_config", "queuereader");
            json = json.get("missing_code_fix");

            String ddsUrl = json.get("dds-ui-url").asText();
            String keyCloakUrl = json.get("keycloak-url").asText();
            String keyCloakRealm = json.get("keycloak-realm").asText();
            String keyCloakUser = json.get("keycloak-username").asText();
            String keyCloakPass = json.get("keycloak-password").asText();
            String keyCloakClientId = json.get("keycloak-client-id").asText();

            KeycloakClient kcClient = new KeycloakClient(keyCloakUrl, keyCloakRealm, keyCloakUser, keyCloakPass, keyCloakClientId);

            WebTarget testTarget = ClientBuilder.newClient().target(ddsUrl).path("api/service/ccgCodes");

            String token = kcClient.getToken().getToken();
            token = JOptionPane.showInputDialog("TOKEN", token);
            LOG.debug("Token = token");

            Invocation.Builder builder = testTarget.request();
            builder = builder.header("Authorization", "Bearer " + token);
            Response response = builder.get();

            *//*Response response = testTarget.
                    .request()
                    .header("Authorization", "Bearer " + kcClient.getToken().getToken())
                    .get();*//*

            //request.Headers.Add("Authorization", this.token);

            int status = response.getStatus();
            LOG.debug("Status = " + status);
            String responseStr = response.readEntity(String.class);
            LOG.debug("responseStr = " + responseStr);

        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /*public static void loadTppStagingData(String odsCode, UUID fromExchange) {
        LOG.debug("Loading TPP Staging Data for " + odsCode + " from Exchange " + fromExchange);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getByLocalIdentifier(odsCode);

            List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
            if (systemIds.size() != 1) {
                throw new Exception("" + systemIds.size() + " system IDs found");
            }
            UUID systemId = systemIds.get(0);

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
            LOG.debug("Got " + exchanges.size() + " exchanges");

            //go backwards, as they're most-recent-first
            for (int i=exchanges.size()-1; i>=0; i--) {
                Exchange exchange = exchanges.get(i);

                if (fromExchange != null) {
                    if (!exchange.getId().equals(fromExchange)) {
                        LOG.debug("Skipping exchange " + exchange.getId());
                        continue;
                    }
                    fromExchange = null;
                }

                LOG.debug("Doing exchange " + exchange.getId() + " from " + exchange.getHeader(HeaderKeys.DataDate));

                Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);

                List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());
                for (ExchangePayloadFile file: files) {

                    String type = file.getType();
                    String filePath = file.getPath();

                    if (type.equals("Ctv3")) {
                        TppCtv3LookupDalI dal = DalProvider.factoryTppCtv3LookupDal();
                        dal.updateLookupTable(filePath, dataDate);

                    } else if (type.equals("Ctv3Hierarchy")) {
                        TppCtv3HierarchyRefDalI dal = DalProvider.factoryTppCtv3HierarchyRefDal();
                        dal.updateHierarchyTable(filePath, dataDate);

                    } else if (type.equals("ImmunisationContent")) {
                        TppImmunisationContentDalI dal = DalProvider.factoryTppImmunisationContentDal();
                        dal.updateLookupTable(filePath, dataDate);

                    } else if (type.equals("ConfiguredListOption")) {
                        TppConfigListOptionDalI dal = DalProvider.factoryTppConfigListOptionDal();
                        dal.updateLookupTable(filePath, dataDate);

                    } else if (type.equals("MedicationReadCodeDetails")) {
                        TppMultiLexToCtv3MapDalI dal = DalProvider.factoryTppMultiLexToCtv3MapDal();
                        dal.updateLookupTable(filePath, dataDate);

                    } else if (type.equals("Mapping")) {
                        TppMappingRefDalI dal = DalProvider.factoryTppMappingRefDal();
                        dal.updateLookupTable(filePath, dataDate);

                    } else if (type.equals("StaffMember")) {

                        TppCsvHelper helper = new TppCsvHelper(service.getId(), systemId, exchange.getId());
                        String[] arr = new String[]{filePath};
                        Map<String, String> versions = TppCsvToFhirTransformer.buildParserToVersionsMap(arr, helper);
                        String version = versions.get(filePath);

                        SRStaffMember parser = new SRStaffMember(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        //bulk load the file into the DB
                        int fileId = parser.getFileAuditId().intValue();
                        TppStaffDalI dal = DalProvider.factoryTppStaffMemberDal();
                        dal.updateStaffMemberLookupTable(filePath, dataDate, fileId);

                    } else if (type.equals("StaffMemberProfile")) {

                        TppCsvHelper helper = new TppCsvHelper(service.getId(), systemId, exchange.getId());
                        String[] arr = new String[]{filePath};
                        Map<String, String> versions = TppCsvToFhirTransformer.buildParserToVersionsMap(arr, helper);
                        String version = versions.get(filePath);

                        SRStaffMemberProfile parser = new SRStaffMemberProfile(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        //bulk load the file into the DB
                        int fileId = parser.getFileAuditId().intValue();
                        TppStaffDalI dal = DalProvider.factoryTppStaffMemberDal();
                        dal.updateStaffProfileLookupTable(filePath, dataDate, fileId);

                    } else {
                        //ignore any other file types
                    }

                }
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    /*public static void loadEmisStagingData(String odsCode, UUID fromExchange) {
        LOG.debug("Loading EMIS Staging Data for " + odsCode + " from Exchange " + fromExchange);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getByLocalIdentifier(odsCode);

            List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
            if (systemIds.size() != 1) {
                throw new Exception("" + systemIds.size() + " system IDs found");
            }
            UUID systemId = systemIds.get(0);

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
            LOG.debug("Got " + exchanges.size() + " exchanges");

            //go backwards, as they're most-recent-first
            for (int i=exchanges.size()-1; i>=0; i--) {
                Exchange exchange = exchanges.get(i);

                if (fromExchange != null) {
                    if (!exchange.getId().equals(fromExchange)) {
                        LOG.debug("Skipping exchange " + exchange.getId());
                        continue;
                    }
                    fromExchange = null;
                }

                LOG.debug("Doing exchange " + exchange.getId() + " from " + exchange.getHeader(HeaderKeys.DataDate));

                Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);

                List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchange.getBody());

                //skip custom extracts
                if (files.size() <= 2) {
                    continue;
                }
                String version = EmisCsvToFhirTransformer.determineVersion(files);

                for (ExchangePayloadFile file: files) {

                    String type = file.getType();
                    String filePath = file.getPath();

                    if (type.equals("Admin_Location")) {

                        Location parser = new Location(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        //the above will have audited the table, so now we can load the bulk staging table with our file
                        EmisLocationDalI dal = DalProvider.factoryEmisLocationDal();
                        int fileId = parser.getFileAuditId().intValue();
                        dal.updateLocationStagingTable(filePath, dataDate, fileId);

                    } else if (type.equals("Admin_OrganisationLocation")) {

                        OrganisationLocation parser = new OrganisationLocation(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        //the above will have audited the table, so now we can load the bulk staging table with our file
                        EmisLocationDalI dal = DalProvider.factoryEmisLocationDal();
                        int fileId = parser.getFileAuditId().intValue();
                        dal.updateOrganisationLocationStagingTable(filePath, dataDate, fileId);

                    } else if (type.equals("Admin_Organisation")) {

                        Organisation parser = new Organisation(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        //the above will have audited the table, so now we can load the bulk staging table with our file
                        EmisOrganisationDalI dal = DalProvider.factoryEmisOrganisationDal();
                        int fileId = parser.getFileAuditId().intValue();
                        dal.updateStagingTable(filePath, dataDate, fileId);

                    } else if (type.equals("Admin_UserInRole")) {

                        UserInRole parser = new UserInRole(service.getId(), systemId, exchange.getId(), version, filePath);
                        while (parser.nextRecord()) {
                            //just spin through it
                        }

                        EmisUserInRoleDalI dal = DalProvider.factoryEmisUserInRoleDal();
                        int fileId = parser.getFileAuditId().intValue();
                        dal.updateStagingTable(filePath, dataDate, fileId);

                    } else if (type.equals("Coding_ClinicalCode")) {

                        EmisCsvHelper helper = new EmisCsvHelper(service.getId(), systemId, exchange.getId(), null, null);
                        ClinicalCode parser = new ClinicalCode(service.getId(), systemId, exchange.getId(), version, filePath);

                        File extraColsFile = ClinicalCodeTransformer.createExtraColsFile(parser, helper);

                        EmisCodeDalI dal = DalProvider.factoryEmisCodeDal();
                        dal.updateClinicalCodeTable(filePath, extraColsFile.getAbsolutePath(), dataDate);

                        FileHelper.deleteRecursiveIfExists(extraColsFile);

                    } else if (type.equals("Coding_DrugCode")) {
                        EmisCodeDalI dal = DalProvider.factoryEmisCodeDal();
                        dal.updateDrugCodeTable(filePath, dataDate);

                    } else {
                        //ignore any other file types
                    }

                }
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    /*public static void requeueSkippedAdminData(boolean tpp, boolean oneAtATime) {
        LOG.debug("Re-queueing skipped admin data for TPP = " + tpp);
        try {
            Connection connection = ConnectionManager.getAuditNonPooledConnection();

            String tagsLike = null;
            if (tpp) {
                tagsLike = "TPP";
            } else {
                tagsLike = "EMIS";
            }

            while (true) {

                String sql = "select s.local_id, a.exchange_id"
                        + " from audit.skipped_admin_data a"
                        + " inner join admin.service s"
                        + " on s.id = a.service_id"
                        + " where s.tags like '%" + tagsLike + "%'"
                        + " and a.queued = false"
                        + " order by a.service_id, a.dt_skipped"
                        + " limit 1";
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                if (!rs.next()) {
                    LOG.debug("Finished");
                    break;
                }

                String odsCode = rs.getString(1);
                UUID firstExchangeId = UUID.fromString(rs.getString(2));
                LOG.debug("Found " + odsCode + " from " + firstExchangeId);

                ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getByLocalIdentifier(odsCode);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("Not one system ID for " + service);
                }
                UUID systemId = systemIds.get(0);

                List<UUID> exchangeIds = new ArrayList<>();

                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                List<UUID> allExchangeIds = exchangeDal.getExchangeIdsForService(service.getId(), systemId);
                int index = allExchangeIds.indexOf(firstExchangeId);
                for (int i=index; i<allExchangeIds.size(); i++) {
                    UUID exchangeId = allExchangeIds.get(i);
                    exchangeIds.add(exchangeId);
                }

                Set<String> fileTypesToFilterOn = new HashSet<>();
                if (tpp) {
                    fileTypesToFilterOn.add("Ctv3");
                    fileTypesToFilterOn.add("Ctv3Hierarchy");
                    fileTypesToFilterOn.add("ImmunisationContent");
                    fileTypesToFilterOn.add("Mapping");
                    fileTypesToFilterOn.add("ConfiguredListOption");
                    fileTypesToFilterOn.add("MedicationReadCodeDetails");
                    fileTypesToFilterOn.add("Ccg");
                    fileTypesToFilterOn.add("Trust");
                    fileTypesToFilterOn.add("Organisation");
                    fileTypesToFilterOn.add("OrganisationBranch");
                    fileTypesToFilterOn.add("StaffMember");
                    fileTypesToFilterOn.add("StaffMemberProfile");


                } else {
                    fileTypesToFilterOn.add("Agreements_SharingOrganisation");
                    fileTypesToFilterOn.add("Admin_OrganisationLocation");
                    fileTypesToFilterOn.add("Admin_Location");
                    fileTypesToFilterOn.add("Admin_Organisation");
                    fileTypesToFilterOn.add("Admin_UserInRole");
                    fileTypesToFilterOn.add("Appointment_SessionUser");
                    fileTypesToFilterOn.add("Appointment_Session");
                    fileTypesToFilterOn.add("Appointment_Slot");
                }

                QueueHelper.postToExchange(exchangeIds, QueueHelper.EXCHANGE_INBOUND, null, true, "Going back to skipped admin", fileTypesToFilterOn, null);

                //update table after re-queuing
                sql = "update audit.skipped_admin_data a"
                        + " inner join admin.service s"
                        + " on s.id = a.service_id"
                        + " set a.queued = true"
                        + " where s.local_id = '" + odsCode + "'";
                statement.executeUpdate(sql);
                connection.commit();

                LOG.debug("Requeued " + exchangeIds.size() + " for " + odsCode);

                if (oneAtATime) {
                    continueOrQuit();
                }
            }

            connection.close();

            LOG.debug("Finished Re-queueing skipped admin data for " + tagsLike);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /**
     * handy fn to stop a routine for manual inspection before continuing (or quitting)
     */
    private static void continueOrQuit() throws Exception {
        LOG.info("Enter y to continue, anything else to quit");

        byte[] bytes = new byte[10];
        System.in.read(bytes);
        char c = (char) bytes[0];
        if (c != 'y' && c != 'Y') {
            System.out.println("Read " + c);
            System.exit(1);
        }
    }

    public static void catptureBartsEncounters(int count, String toFile) {
        LOG.debug("Capturing " + count + " Barts Encounters to " + toFile);
        try {
            UUID serviceId = UUID.fromString("b5a08769-cbbe-4093-93d6-b696cd1da483");
            UUID systemId = UUID.fromString("d874c58c-91fd-41bb-993e-b1b8b22038b2");

            File dstFile = new File(toFile);
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //the Emis records use Windows record separators, so we need to match that otherwise
            //the bulk import routine will fail
            CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                    .withHeader("patientId", "episodeId", "id", "startDateDesc", "endDateDesc", "messageTypeCode",
                            "messageTypeDesc", "statusDesc", "statusHistorySize", "classDesc", "typeDesc",
                            "practitionerId", "dtRecordedDesc", "exchangeDateDesc", "currentLocation",
                            "locationHistorySize", "serviceProvider", "cegEnterpriseId", "bhrEnterpriseId", "json"
                    );


            CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, count);
            LOG.debug("Found " + count + " exchanges");

            int done = 0;
            for (Exchange exchange: exchanges) {

                String body = exchange.getBody();

                try {
                    Bundle bundle = (Bundle)FhirResourceHelper.deserialiseResouce(body);
                    List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
                    for (Bundle.BundleEntryComponent entry: entries) {
                        Resource resource = entry.getResource();
                        if (resource instanceof Encounter) {

                            Encounter encounter = (Encounter)resource;

                            String id = encounter.getId();

                            String dtRecordedDesc = null;
                            DateTimeType dtRecorded = (DateTimeType)ExtensionConverter.findExtensionValue(encounter, FhirExtensionUri.RECORDED_DATE);
                            if (dtRecorded != null) {
                                Date d = dtRecorded.getValue();
                                dtRecordedDesc = sdf.format(d);
                            }

                            String messageTypeDesc = null;
                            String messageTypeCode = null;
                            CodeableConcept messageTypeConcept = (CodeableConcept)ExtensionConverter.findExtensionValue(encounter, FhirExtensionUri.HL7_MESSAGE_TYPE);
                            if (messageTypeConcept != null) {
                                messageTypeDesc = messageTypeConcept.getText();

                                if (messageTypeConcept.hasCoding()) {
                                    Coding coding = messageTypeConcept.getCoding().get(0);
                                    messageTypeCode = coding.getCode();
                                }
                            }

                            String statusDesc = null;
                            if (encounter.hasStatus()) {
                                statusDesc = "" + encounter.getStatus();
                            }

                            Integer statusHistorySize = 0;
                            if (encounter.hasStatusHistory()) {
                                statusHistorySize = new Integer(encounter.getStatusHistory().size());
                            }

                            String classDesc = null;
                            if (encounter.hasClass_()) {
                                classDesc = "" + encounter.getClass_();
                            }

                            String typeDesc = null;
                            if (encounter.hasType()) {
                                List<CodeableConcept> types = encounter.getType();
                                CodeableConcept type = types.get(0);
                                typeDesc = type.getText();
                            }

                            String patientId = null;
                            if (encounter.hasPatient()) {
                                Reference ref = encounter.getPatient();
                                patientId = ReferenceHelper.getReferenceId(ref);
                            }

                            String episodeId = null;
                            if (encounter.hasEpisodeOfCare()) {
                                List<Reference> refs = encounter.getEpisodeOfCare();
                                Reference ref = refs.get(0);
                                episodeId = ReferenceHelper.getReferenceId(ref);
                            }

                            String practitionerId = null;
                            if (encounter.hasParticipant()) {
                                List<Encounter.EncounterParticipantComponent> parts = encounter.getParticipant();
                                Encounter.EncounterParticipantComponent part = parts.get(0);
                                if (part.hasIndividual()) {
                                    Reference ref = part.getIndividual();
                                    practitionerId = ReferenceHelper.getReferenceId(ref);
                                }
                            }

                            String startDateDesc = null;
                            String endDateDesc = null;
                            if (encounter.hasPeriod()) {
                                Period p = encounter.getPeriod();
                                if (p.hasStart()) {
                                    startDateDesc = sdf.format(p.getStart());
                                }
                                if (p.hasEnd()) {
                                    endDateDesc = sdf.format(p.getEnd());
                                }
                            }

                            Date dataDate = exchange.getHeaderAsDate(HeaderKeys.DataDate);
                            String exchangeDateDesc = sdf.format(dataDate);

                            Integer locationHistorySize = 0;
                            String currentLocation = null;
                            if (encounter.hasLocation()) {
                                locationHistorySize = new Integer(encounter.getLocation().size());

                                for (Encounter.EncounterLocationComponent loc: encounter.getLocation()) {
                                    if (loc.getStatus() == Encounter.EncounterLocationStatus.ACTIVE) {
                                        Reference ref = loc.getLocation();
                                        currentLocation = ReferenceHelper.getReferenceId(ref);
                                    }
                                }
                            }

                            String serviceProvider = null;
                            if (encounter.hasServiceProvider()) {
                                Reference ref = encounter.getServiceProvider();
                                serviceProvider = ReferenceHelper.getReferenceId(ref);
                            }

                            SubscriberResourceMappingDalI dal = DalProvider.factorySubscriberResourceMappingDal("ceg_enterprise");
                            Long cegEnterpriseId = dal.findEnterpriseIdOldWay(ResourceType.Encounter.toString(), id);

                            dal = DalProvider.factorySubscriberResourceMappingDal("pcr_01_enterprise_pi");
                            Long bhrEnterpriseId = dal.findEnterpriseIdOldWay(ResourceType.Encounter.toString(), id);

                            String json = FhirSerializationHelper.serializeResource(encounter);

                            printer.printRecord(patientId, episodeId, id, startDateDesc, endDateDesc, messageTypeCode,
                                    messageTypeDesc, statusDesc, statusHistorySize, classDesc, typeDesc,
                                    practitionerId, dtRecordedDesc, exchangeDateDesc, currentLocation,
                                    locationHistorySize, serviceProvider, cegEnterpriseId, bhrEnterpriseId, json);

                        }
                    }

                } catch (Exception ex) {
                    throw new Exception("Exception on exchange " + exchange.getId(), ex);
                }

                done ++;
                if (done % 100 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            printer.close();

            LOG.debug("Finished Capturing Barts Encounters to " + dstFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void transformAdtEncounters(String odsCode, String tableName) {
        LOG.debug("Transforming " + odsCode + " Encounters from " + tableName);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getByLocalIdentifier(odsCode);
            LOG.debug("Running for " + service);

            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date from = sdf.parse(dFrom);
            Date to = sdf.parse(dTo);*/

            UUID serviceId = service.getId();
            UUID systemId = UUID.fromString("d874c58c-91fd-41bb-993e-b1b8b22038b2");

            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            int done = 0;

            while (true) {

                Connection connection = ConnectionManager.getEdsNonPooledConnection();
                Statement statement = connection.createStatement();

                List<UUID> ids = new ArrayList<>();

                ResultSet rs = statement.executeQuery("SELECT exchange_id FROM " + tableName + " WHERE done = 0 ORDER BY timestamp LIMIT 1000");
                while (rs.next()) {
                    UUID exchangeId = UUID.fromString(rs.getString(1));
                    ids.add(exchangeId);

                    Exchange exchange = exchangeDal.getExchange(exchangeId);
                    String body = exchange.getBody();

                    try {
                        if (!body.equals("[]")) {
                            Bundle bundle = (Bundle) FhirResourceHelper.deserialiseResouce(body);
                            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
                            for (Bundle.BundleEntryComponent entry : entries) {
                                Resource resource = entry.getResource();
                                if (resource instanceof Encounter) {

                                    Encounter encounter = (Encounter) resource;

                                    ResourceWrapper currentWrapper = resourceDal.getCurrentVersion(serviceId, encounter.getResourceType().toString(), UUID.fromString(encounter.getId()));
                                    if (currentWrapper != null
                                            && !currentWrapper.isDeleted()) {

                                        List<UUID> batchIdsCreated = new ArrayList<>();
                                        TransformError transformError = new TransformError();
                                        FhirResourceFiler innerFiler = new FhirResourceFiler(exchange.getId(), serviceId, systemId, transformError, batchIdsCreated);
                                        FhirHl7v2Filer.AdtResourceFiler filer = new FhirHl7v2Filer.AdtResourceFiler(innerFiler);

                                        ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
                                        transformAudit.setServiceId(serviceId);
                                        transformAudit.setSystemId(systemId);
                                        transformAudit.setExchangeId(exchange.getId());
                                        transformAudit.setId(UUID.randomUUID());
                                        transformAudit.setStarted(new Date());

                                        AuditWriter.writeExchangeEvent(exchange.getId(), "Re-transforming Encounter for encounter_event");

                                        //actually call the transform code
                                        Encounter currentEncounter = (Encounter) currentWrapper.getResource();
                                        EncounterTransformer.updateEncounter(currentEncounter, encounter, filer);

                                        innerFiler.waitToFinish();

                                        transformAudit.setEnded(new Date());
                                        transformAudit.setNumberBatchesCreated(new Integer(batchIdsCreated.size()));

                                        if (transformError.getError().size() > 0) {
                                            transformAudit.setErrorXml(TransformErrorSerializer.writeToXml(transformError));
                                        }
                                        exchangeDal.save(transformAudit);

                                        if (!transformError.getError().isEmpty()) {
                                            throw new Exception("Had error on Exchange " + exchange.getId());
                                        }

                                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                                        exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                                        //send on to protocol queue
                                        PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig("EdsProtocol");
                                        PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
                                        component.process(exchange);
                                    }
                                }
                            }
                        }

                        //mark as done
                        Connection connection2 = ConnectionManager.getEdsConnection();
                        PreparedStatement ps = connection2.prepareStatement("UPDATE " + tableName + " SET done = 1 WHERE exchange_id = ?");
                        ps.setString(1, exchangeId.toString());
                        ps.executeUpdate();
                        connection2.commit();
                        ps.close();
                        connection2.close();

                    } catch (Exception ex) {
                        throw new Exception("Exception on exchange " + exchange.getId(), ex);
                    }

                    done++;
                    if (done % 100 == 0) {
                        LOG.debug("Done " + done);
                    }

                }

                statement.close();
                connection.close();

                if (ids.isEmpty()) {
                    break;
                }
            }
            LOG.debug("Done " + done);

            LOG.debug("Finished Transforming Barts Encounters from " + tableName);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void findEmisServicesNeedReprocessing(String odsCodeRegex) {
        LOG.debug("Finding Emis Services that Need Re-processing for " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("Wrong number of system IDs for " + service);
                }
                UUID systemId = systemIds.get(0);

                String publisherStatus = null;
                for (ServiceInterfaceEndpoint serviceInterface: service.getEndpointsList()) {
                    if (serviceInterface.getSystemUuid().equals(systemId)) {
                        publisherStatus = serviceInterface.getEndpoint();
                    }
                }

                if (publisherStatus == null) {
                    throw new Exception("Failed to find publisher status for service " + service);
                }

                LOG.debug("");
                LOG.debug("CHECKING " + service + " " + publisherStatus + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                if (publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL)) {
                    LOG.debug("Skipping service because set to auto-fail");
                    continue;
                }

                //check if in the bulks skipped table and are waiting to be re-queued
                Connection connection = ConnectionManager.getEdsConnection();
                String sql = "select 1 from audit.skipped_exchanges_left_and_dead where ods_code = ? and (queued = false or queued is null)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, service.getLocalId());
                ResultSet rs = ps.executeQuery();
                boolean needsRequeueing = rs.next();
                ps.close();
                connection.close();
                if (needsRequeueing) {
                    LOG.debug(">>>>> NEEDS REQUEUEING FOR SKIPPED BULK");
                }

                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                for (int i=exchanges.size()-1; i>=0; i--) {
                    Exchange exchange = exchanges.get(i);

                    //if can't be queued, ignore it
                    Boolean allowQueueing = exchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
                    if (allowQueueing != null
                            && !allowQueueing.booleanValue()) {
                        continue;
                    }

                    //skip any custom extracts
                    String body = exchange.getBody();
                    List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body, false);
                    if (files.size() <= 1) {
                        continue;
                    }

                    //LOG.debug("Doing exchange " + exchange.getId() + " from " + exchange.getHeaderAsDate(HeaderKeys.DataDate));

                    List<ExchangeTransformAudit> audits = exchangeDal.getAllExchangeTransformAudits(service.getId(), systemId, exchange.getId());
                    List<ExchangeEvent> events = exchangeDal.getExchangeEvents(exchange.getId());

                    //was it transformed OK before it was re-queued with filtering?
                    boolean transformedWithoutFiltering = false;
                    List<String> logging = new ArrayList<>();

                    for (ExchangeTransformAudit audit: audits) {

                        //transformed OK
                        boolean transformedOk = audit.getEnded() != null && audit.getErrorXml() == null;
                        if (!transformedOk) {
                            logging.add("Audit " + audit.getStarted() + " didn't complete OK, so not counting");
                            continue;
                        }

                        //if transformed OK see whether filtering was applied BEFORE
                        Date dtTransformStart = audit.getStarted();
                        logging.add("Audit completed OK from " + audit.getStarted());

                        //find immediately proceeding event showing loading into inbound queue
                        ExchangeEvent previousLoadingEvent = null;
                        for (int j=events.size()-1; j>=0; j--) {
                            ExchangeEvent event = events.get(j);
                            Date dtEvent = event.getTimestamp();
                            if (dtEvent.after(dtTransformStart)) {
                                logging.add("Ignoring event from " + dtEvent + " as AFTER transform");
                                continue;
                            }

                            String eventDesc = event.getEventDesc();
                            if (eventDesc.startsWith("Manually pushed into edsInbound exchange")
                                    || eventDesc.startsWith("Manually pushed into EdsInbound exchange")) {
                                previousLoadingEvent = event;
                                logging.add("Proceeding event from " + dtEvent + " [" + eventDesc + "]");
                                break;
                            } else {
                                logging.add("Ignoring event from " + dtEvent + " as doesn't match text [" + eventDesc + "]");
                            }
                        }

                        if (previousLoadingEvent == null) {
                            //if transformed OK and no previous manual loading event, then it was OK
                            transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK without being manually loaded = OK");

                        } else {
                            //if transformed OK and was manually loaded into queue, then see if event applied filtering or not
                            String eventDesc = previousLoadingEvent.getEventDesc();
                            if (!eventDesc.contains("Filtered on file types")) {
                                transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK and was manually loaded without filtering = OK");

                            } else {
                                logging.add("Event desc filters on file types, so DIDN'T transform OK");
                            }
                        }
                    }

                    if (!transformedWithoutFiltering) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        LOG.error("" + service + " -> exchange " + exchange.getId() + " from " + sdf.format(exchange.getHeaderAsDate(HeaderKeys.DataDate)));
                        /*for (String line: logging) {
                            LOG.error("    " + line);
                        }*/
                    }
                }
            }

            LOG.debug("Finished Finding Emis Services that Need Re-processing");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    // For the protocol name provided, get the list of services which are publishers and send
    // their transformed Patient and EpisodeOfCare FHIR resources to the Enterprise Filer
    public static void transformAndFilePatientsAndEpisodesForProtocolServices (String compassVersion, String protocolName, String subscriberConfigName) throws Exception {

        //find the protocol using the name parameter
        LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);
        if (matchedLibraryItem == null) {
            System.out.println("Protocol not found : " + protocolName);
            return;
        }
        UUID protocolUuid = UUID.fromString(matchedLibraryItem.getUuid());
        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

        //get all the active publishing services for this protocol
        List<ServiceContract> serviceContracts = matchedLibraryItem.getProtocol().getServiceContract();
        for (ServiceContract serviceContract : serviceContracts) {
            if (serviceContract.getType().equals(PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                UUID serviceUuid = UUID.fromString(serviceContract.getService().getUuid());

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUuid, true);
                for (UUID patientId : patientIds) {

                    List<ResourceWrapper> patientResources = new ArrayList<>();
                    UUID batchUuid = UUID.randomUUID();

                    //need the Patient and the EpisodeOfCare resources for each service patient
                    ResourceWrapper patientWrapper
                            = dal.getCurrentVersion(serviceUuid, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null) {
                        LOG.warn("Null patient resource for Patient " + patientId);
                        continue;
                    }
                    patientResources.add(patientWrapper);

                    if (compassVersion.equalsIgnoreCase(COMPASS_V1)) {
                        String patientContainerString
                                = BulkHelper.getEnterpriseContainerForPatientData(patientResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (patientContainerString != null) {
                            EnterpriseFiler.file(batchUuid, UUID.randomUUID(), patientContainerString, subscriberConfigName);
                        }
                    } else if (compassVersion.equalsIgnoreCase(COMPASS_V2)) {
                        String patientContainerString
                                = BulkHelper.getSubscriberContainerForPatientData(patientResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (patientContainerString != null) {
                            SubscriberFiler.file(batchUuid, UUID.randomUUID(), patientContainerString, subscriberConfigName);
                        }
                    }

                    List<ResourceWrapper> episodeResources = new ArrayList<>();

                    //patient may have multiple episodes of care at the service, so pass them in
                    List<ResourceWrapper> episodeWrappers
                            = dal.getResourcesByPatient(serviceUuid, patientId, ResourceType.EpisodeOfCare.toString());

                    if (episodeWrappers.isEmpty()) {
                        LOG.warn("No episode resources for Patient " + patientId);
                        continue;
                    }
                    for (ResourceWrapper episodeWrapper: episodeWrappers  ) {
                        episodeResources.add(episodeWrapper);
                    }

                    if (compassVersion.equalsIgnoreCase(COMPASS_V1)) {
                        String episodeContainerString
                                = BulkHelper.getEnterpriseContainerForEpisodeData(episodeResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (episodeContainerString != null) {
                            EnterpriseFiler.file(batchUuid, UUID.randomUUID(), episodeContainerString, subscriberConfigName);
                        }
                    } else if (compassVersion.equalsIgnoreCase(COMPASS_V2)) {
                        String episodeContainerString
                                = BulkHelper.getEnterpriseContainerForEpisodeData(episodeResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (episodeContainerString != null) {
                            SubscriberFiler.file(batchUuid, UUID.randomUUID(), episodeContainerString, subscriberConfigName);
                        }
                    }


                }
            }
        }
    }

    /**
     * we've found that the TPP data contains registration data for other organisations, which
     * is causing a lot of confusion (and potential duplication). The transform now doesn't process these
     * records, and this routine will tidy up any existing data
     */
    public static void deleteTppEpisodesElsewhere(String odsCodeRegex, boolean testMode) {
        LOG.debug("Deleting TPP Episodes Elsewhere for " + odsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("Wrong number of system IDs for " + service);
                }
                UUID systemId = systemIds.get(0);

                PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
                ResourceDalI resourceDal = DalProvider.factoryResourceDal();

                List<UUID> patientIds = patientSearchDal.getPatientIds(service.getId(), false);
                LOG.debug("Found " + patientIds.size());


                //create dummy exchange
                String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                String odsCode = service.getLocalId();

                Exchange exchange = null;
                UUID exchangeId = UUID.randomUUID();

                List<UUID> batchIdsCreated = new ArrayList<>();
                FhirResourceFiler filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                int deleted = 0;

                for (int i=0; i<patientIds.size(); i++) {
                    UUID patientId = patientIds.get(i);

                    Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(service.getId(), ResourceType.Patient, patientId.toString());
                    if (patient == null) {
                        continue;
                    }
                    if (!patient.hasManagingOrganization()) {
                        throw new Exception("No managing organization on Patient " + patientId);
                    }
                    Reference patientManagingOrgRef = patient.getManagingOrganization();

                    List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.EpisodeOfCare.toString());
                    for (ResourceWrapper wrapper: wrappers) {
                        if (wrapper.isDeleted()) {
                            throw new Exception("Unexpected deleted resource " + wrapper.getResourceType() + " " + wrapper.getResourceId());
                        }
                        EpisodeOfCare episodeOfCare = (EpisodeOfCare)wrapper.getResource();
                        if (!episodeOfCare.hasManagingOrganization()) {
                            throw new Exception("No managing organization on Episode " + episodeOfCare.getId());
                        }
                        Reference episodeManagingOrgRef = episodeOfCare.getManagingOrganization();
                        if (!ReferenceHelper.equals(patientManagingOrgRef, episodeManagingOrgRef)) {

                            deleted ++;

                            //delete this episode
                            if (testMode) {
                                LOG.debug("Would delete episode " + episodeOfCare.getId());

                            } else {

                                if (exchange == null) {
                                    exchange = new Exchange();
                                    exchange.setId(exchangeId);
                                    exchange.setBody(bodyJson);
                                    exchange.setTimestamp(new Date());
                                    exchange.setHeaders(new HashMap<>());
                                    exchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
                                    exchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
                                    exchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
                                    exchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
                                    exchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.TPP_CSV);
                                    exchange.setServiceId(service.getId());
                                    exchange.setSystemId(systemId);

                                    AuditWriter.writeExchange(exchange);
                                    AuditWriter.writeExchangeEvent(exchange, "Manually created to delete Episodes at other organisations");
                                }

                                //delete resource
                                filer.deletePatientResource(null, false, new EpisodeOfCareBuilder(episodeOfCare));

                            }
                        }
                    }

                    if (i % 1000 == 0) {
                        LOG.debug("Done " + i);
                    }
                }

                LOG.debug("Finished processing " + patientIds.size() + " patients and deleted " + deleted + " episodes");

                if (!testMode) {

                    //close down filer
                    filer.waitToFinish();

                    if (exchange != null) {
                        //set multicast header
                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                        exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                        //post to Rabbit protocol queue
                        List<UUID> exchangeIds = new ArrayList<>();
                        exchangeIds.add(exchange.getId());
                        QueueHelper.postToExchange(exchangeIds, "EdsProtocol", null, true, null);

                        //set the flag to prevent any re-queuing
                        exchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                        AuditWriter.writeExchange(exchange);
                    }
                }
            }

            LOG.debug("Finished Deleting TPP Episodes Elsewhere for " + odsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void postToInboundFromFile(String filePath, String reason) {

        try {
            LOG.info("Posting to inbound exchange from file " + filePath);

            //read in file into map keyed by service and system
            Map<UUID, Map<UUID, List<UUID>>> hmExchangeIds = new HashMap<>();

            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);

            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            ServiceDalI serviceDalI = DalProvider.factoryServiceDal();

            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                UUID exchangeId = UUID.fromString(line);
                Exchange exchange = exchangeDal.getExchange(exchangeId);
                UUID serviceId = exchange.getServiceId();
                UUID systemId = exchange.getSystemId();

                Map<UUID, List<UUID>> inner = hmExchangeIds.get(serviceId);
                if (inner == null) {
                    inner = new HashMap<>();
                    hmExchangeIds.put(serviceId, inner);
                }

                List<UUID> list = inner.get(systemId);
                if (list == null) {
                    list = new ArrayList<>();
                    inner.put(systemId, list);
                }
                list.add(exchangeId);
            }
            br.close();

            LOG.debug("Found exchanges for " + hmExchangeIds.size() + " services");

            for (UUID serviceId: hmExchangeIds.keySet()) {
                Map<UUID, List<UUID>> inner = hmExchangeIds.get(serviceId);

                Service service = serviceDalI.getById(serviceId);
                LOG.debug("Doing " + service);

                for (UUID systemId: inner.keySet()) {

                    List<UUID> exchangeIds = inner.get(systemId);

                    int count = 0;
                    List<UUID> exchangeIdBatch = new ArrayList<>();

                    for (UUID exchangeId : exchangeIds) {

                        count++;
                        exchangeIdBatch.add(exchangeId);

                        //update the transform audit, so EDS UI knows we've re-queued this exchange
                        ExchangeTransformAudit audit = exchangeDal.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
                        if (audit != null
                                && !audit.isResubmitted()) {
                            audit.setResubmitted(true);
                            exchangeDal.save(audit);
                        }

                        if (exchangeIdBatch.size() >= 1000) {
                            QueueHelper.postToExchange(exchangeIdBatch, "EdsInbound", null, false, reason);
                            exchangeIdBatch = new ArrayList<>();
                            LOG.info("Done " + count);
                        }
                    }

                    if (!exchangeIdBatch.isEmpty()) {
                        QueueHelper.postToExchange(exchangeIdBatch, "EdsInbound", null, false, reason);
                        LOG.info("Done " + count);
                    }
                }
            }

            LOG.info("Finished Posting to inbound");
        } catch (Throwable ex) {
            LOG.error("", ex);
        }
    }

    public static void deleteDataFromOldCoreDB(UUID serviceId, String previousPublisherConfigName) {
        LOG.debug("Deleting data from old Core DB server for " + serviceId);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getById(serviceId);
            LOG.debug("Service = " + service);

            //find a service on the OLD config DB
            UUID altServiceId = null;
            for (Service s: serviceDal.getAll()) {
                if (s.getPublisherConfigName() != null
                        && s.getPublisherConfigName().equals(previousPublisherConfigName)) {
                    altServiceId = s.getId();
                    break;
                }
            }
            if (altServiceId == null) {
                throw new Exception("Failed to find any service on publisher " + previousPublisherConfigName);
            }

            //find all subscriber config names
            Set<String> subscriberConfigNames = new HashSet<>();

            List<String> protocolIds = DetermineRelevantProtocolIds.getProtocolIdsForPublisherServiceOldWay(serviceId.toString());
            for (String oldProtocolId: protocolIds) {
                LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(UUID.fromString(oldProtocolId));
                Protocol protocol = libraryItem.getProtocol();

                //skip disabled protocols
                if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
                    continue;
                }

                List<ServiceContract> subscribers = protocol
                        .getServiceContract()
                        .stream()
                        .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                        .filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
                        .collect(Collectors.toList());

                for (ServiceContract serviceContract : subscribers) {
                    String subscriberConfigName = MessageTransformOutbound.getSubscriberEndpoint(serviceContract);
                    subscriberConfigNames.add(subscriberConfigName);
                }
            }
            LOG.debug("Found " + subscriberConfigNames.size() + " subscribers: " + subscriberConfigNames);

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, true);
            LOG.debug("Found " + patientIds.size());
            patientIds.add(null); //for admin resources

            Connection ehrConnection = ConnectionManager.getEhrNonPooledConnection(altServiceId);

            int done = 0;
            for (UUID patientId: patientIds) {

                //send to each subscriber
                for (String subscriberConfigName: subscriberConfigNames) {

                    SubscriberResourceMappingDalI subscriberDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
                    OutputContainer output = new OutputContainer();
                    boolean doneSomething = false;

                    String sql = "SELECT resource_id, resource_type"
                            + " FROM resource_current"
                            + " WHERE service_id = ?"
                            + " AND patient_id = ?";
                    PreparedStatement ps = ehrConnection.prepareStatement(sql);
                    ps.setFetchSize(500);
                    ps.setString(1, serviceId.toString());
                    if (patientId == null) {
                        ps.setString(2, ""); //get admin data
                    } else {
                        ps.setString(2, patientId.toString());
                    }

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {

                        String resourceId = rs.getString(1);
                        String resourceType = rs.getString(2);
                        String sourceId = ReferenceHelper.createResourceReference(resourceType, resourceId);
                        SubscriberTableId subscriberTableId = null;

                        if (resourceType.equals("Patient")) {
                            subscriberTableId = SubscriberTableId.PATIENT;
                        } else if (resourceType.equals("AllergyIntolerance")) {
                            subscriberTableId = SubscriberTableId.ALLERGY_INTOLERANCE;
                        } else if (resourceType.equals("Encounter")) {
                            subscriberTableId = SubscriberTableId.ENCOUNTER;
                        } else if (resourceType.equals("EpisodeOfCare")) {
                            subscriberTableId = SubscriberTableId.EPISODE_OF_CARE;
                        } else if (resourceType.equals("Flag")) {
                            subscriberTableId = SubscriberTableId.FLAG;
                        } else if (resourceType.equals("Location")) {
                            subscriberTableId = SubscriberTableId.LOCATION;
                        } else if (resourceType.equals("MedicationOrder")) {
                            subscriberTableId = SubscriberTableId.MEDICATION_ORDER;
                        } else if (resourceType.equals("MedicationStatement")) {
                            subscriberTableId = SubscriberTableId.MEDICATION_STATEMENT;
                        } else if (resourceType.equals("Observation")
                                || resourceType.equals("Condition")
                                || resourceType.equals("Immunization")
                                || resourceType.equals("FamilyMemberHistory")) {
                            subscriberTableId = SubscriberTableId.OBSERVATION;
                        } else if (resourceType.equals("Organization")) {
                            subscriberTableId = SubscriberTableId.ORGANIZATION;
                        } else if (resourceType.equals("Practitioner")) {
                            subscriberTableId = SubscriberTableId.PRACTITIONER;
                        } else if (resourceType.equals("ProcedureRequest")) {
                            subscriberTableId = SubscriberTableId.PROCEDURE_REQUEST;
                        } else if (resourceType.equals("ReferralRequest")) {
                            subscriberTableId = SubscriberTableId.REFERRAL_REQUEST;
                        } else if (resourceType.equals("Schedule")) {
                            subscriberTableId = SubscriberTableId.SCHEDULE;
                        } else if (resourceType.equals("Appointment")) {
                            subscriberTableId = SubscriberTableId.APPOINTMENT;
                        } else if (resourceType.equals("DiagnosticOrder")) {
                            subscriberTableId = SubscriberTableId.DIAGNOSTIC_ORDER;
                        } else if (resourceType.equals("Slot")) {
                            //these were ignored
                            continue;
                        } else {
                            throw new Exception("Unexpected resource type " + resourceType + " " + resourceId);
                        }


                        SubscriberId subscriberId = subscriberDal.findSubscriberId(subscriberTableId.getId(), sourceId);
                        if (subscriberId == null) {
                            continue;
                        }

                        doneSomething = true;

                        if (resourceType.equals("Patient")) {
                            output.getPatients().writeDelete(subscriberId);

                            //the database doesn't have any pseudo IDs, so don't need to worry about that table

                            int maxAddresses = 0;
                            int maxTelecoms = 0;
                            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                            List<ResourceWrapper> history = resourceDal.getResourceHistory(altServiceId, resourceType, UUID.fromString(resourceId));
                            for (ResourceWrapper h: history) {
                                Patient p = (Patient)h.getResource();
                                if (p != null) {
                                    maxAddresses = Math.max(maxAddresses, p.getAddress().size());
                                    maxTelecoms = Math.max(maxTelecoms, p.getTelecom().size());
                                }
                            }
                            for (int i=0; i<maxAddresses; i++) {
                                String subSourceId = sourceId + "-ADDR-" + i;
                                SubscriberId subTableId = subscriberDal.findSubscriberId(SubscriberTableId.PATIENT_ADDRESS.getId(), subSourceId);
                                if (subTableId != null) {
                                    output.getPatientAddresses().writeDelete(subTableId);
                                }
                            }
                            for (int i=0; i<maxTelecoms; i++) {
                                String subSourceId = sourceId + "-TELECOM-" + i;
                                SubscriberId subTableId = subscriberDal.findSubscriberId(SubscriberTableId.PATIENT_CONTACT.getId(), subSourceId);
                                if (subTableId != null) {
                                    output.getPatientContacts().writeDelete(subTableId);
                                }
                            }

                        } else if (resourceType.equals("AllergyIntolerance")) {
                            output.getAllergyIntolerances().writeDelete(subscriberId);
                        } else if (resourceType.equals("Encounter")) {
                            output.getEncounters().writeDelete(subscriberId);
                        } else if (resourceType.equals("EpisodeOfCare")) {
                            output.getEpisodesOfCare().writeDelete(subscriberId);
                            //reg status history table isn't populated yet, so can ignore that
                        } else if (resourceType.equals("Flag")) {
                            output.getFlags().writeDelete(subscriberId);
                        } else if (resourceType.equals("Location")) {
                            output.getLocations().writeDelete(subscriberId);
                        } else if (resourceType.equals("MedicationOrder")) {
                            output.getMedicationOrders().writeDelete(subscriberId);
                        } else if (resourceType.equals("MedicationStatement")) {
                            output.getMedicationStatements().writeDelete(subscriberId);
                        } else if (resourceType.equals("Observation")
                                || resourceType.equals("Condition")
                                || resourceType.equals("Immunization")
                                || resourceType.equals("FamilyMemberHistory")) {
                            output.getObservations().writeDelete(subscriberId);
                        } else if (resourceType.equals("Organization")) {
                            output.getOrganisations().writeDelete(subscriberId);
                        } else if (resourceType.equals("Practitioner")) {
                            output.getPractitioners().writeDelete(subscriberId);
                        } else if (resourceType.equals("ProcedureRequest")) {
                            output.getProcedureRequests().writeDelete(subscriberId);
                        } else if (resourceType.equals("ReferralRequest")) {
                            output.getReferralRequests().writeDelete(subscriberId);
                        } else if (resourceType.equals("Schedule")) {
                            output.getSchedules().writeDelete(subscriberId);
                        } else if (resourceType.equals("Appointment")) {
                            output.getAppointments().writeDelete(subscriberId);
                        } else if (resourceType.equals("DiagnosticOrder")) {
                            output.getDiagnosticOrder().writeDelete(subscriberId);
                        } else {
                            throw new Exception("Unexpected resource type " + resourceType + " " + resourceId);
                        }
                    }

                    ps.close();

                    if (doneSomething) {

                        byte[] bytes = output.writeToZip();
                        String base64 = Base64.getEncoder().encodeToString(bytes);

                        UUID batchId = UUID.randomUUID();

                        SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
                    }
                }

                done ++;
                if (done % 100 == 0) {
                    LOG.debug("Done " + done);
                }
            }
            LOG.debug("Done " + done);

            ehrConnection.close();

            LOG.debug("Finished Deleting data from old Core DB server for " + serviceId);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void populateMissingOrgsInCompassV1(String subscriberConfigName, boolean testMode) {
        LOG.debug("Populating Missing Orgs In CompassV1 " + subscriberConfigName + " testMode = " + testMode);
        try {

            Connection subscriberTransformConnection = ConnectionManager.getSubscriberTransformNonPooledConnection(subscriberConfigName);

            //get orgs in that subscriber DB
            Map<UUID, Long> hmOrgs = new HashMap<>();

            String sql = "SELECT service_id, enterprise_id FROM enterprise_organisation_id_map";
            Statement statement = subscriberTransformConnection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String s = rs.getString(1);
                long id = rs.getLong(2);
                hmOrgs.put(UUID.fromString(s), new Long(id));
            }
            LOG.debug("Found " + hmOrgs.size() + " orgs");

            for (UUID serviceId: hmOrgs.keySet()) {
                Long enterpriseId = hmOrgs.get(serviceId);
                LOG.debug("Doing service " + serviceId + ", enterprise ID " + enterpriseId);

                /*ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getById(serviceId);*/

                //find the FHIR Organization this is from
                sql = "SELECT resource_id FROM enterprise_id_map WHERE enterprise_id = " + enterpriseId;
                rs = statement.executeQuery(sql);
                if (!rs.next()) {
                    sql = "SELECT resource_id FROM enterprise_id_map_3 WHERE enterprise_id = " + enterpriseId;
                    rs = statement.executeQuery(sql);
                    if (!rs.next()) {
                        throw new Exception("Failed to find resource ID for service ID " + serviceId + " and enterprise ID " + enterpriseId);
                    }
                }

                String resourceId = rs.getString(1);
                Reference orgRef = ReferenceHelper.createReference(ResourceType.Organization, resourceId);

                //make sure our org is on the CoreXX server we expect and take over any instance mapping
                orgRef = findNewOrgRefOnCoreDb(subscriberConfigName, orgRef, serviceId, testMode);
                if (orgRef == null) {
                    LOG.warn("<<<<<<<<No Organization resource could be found for " + resourceId + ">>>>>>>>>>>>>>>>>>>>>>>>>");
                    continue;
                }

                //find the org and work up to find all its parents too
                List<ResourceWrapper> resourceWrappers = new ArrayList<>();

                while (orgRef != null) {

                    ReferenceComponents comps = ReferenceHelper.getReferenceComponents(orgRef);
                    if (comps.getResourceType() != ResourceType.Organization) {
                        throw new Exception("Found non-organisation resource mapping for enterprise ID " + enterpriseId + ": " + resourceId);
                    }
                    UUID orgId = UUID.fromString(comps.getId());

                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Organization.toString(), orgId);
                    if (wrapper == null) {
                        throw new Exception("Failed to find resource wrapper for parent org with resource ID " + resourceId);
                    }

                    resourceWrappers.add(wrapper);

                    Organization org = (Organization)wrapper.getResource();
                    if (org.hasPartOf()) {
                        orgRef = org.getPartOf();
                    } else {
                        orgRef = null;
                    }
                }

                if (testMode) {
                    LOG.debug("Found " + resourceWrappers.size() + " orgs");
                    for (ResourceWrapper wrapper: resourceWrappers) {
                        Organization org = (Organization)wrapper.getResource();
                        String odsCode = IdentifierHelper.findOdsCode(org);
                        LOG.debug("    Got " + org.getName() + " [" + odsCode + "]");
                    }

                } else {
                    EnterpriseTransformHelper helper = new EnterpriseTransformHelper(serviceId, null, null, null, subscriberConfigName, resourceWrappers, false);
                    org.endeavourhealth.transform.enterprise.outputModels.Organization orgWriter = helper.getOutputContainer().getOrganisations();

                    OrganisationEnterpriseTransformer t = new OrganisationEnterpriseTransformer();
                    t.transformResources(resourceWrappers, orgWriter, helper);

                    org.endeavourhealth.transform.enterprise.outputModels.OutputContainer output = helper.getOutputContainer();
                    byte[] bytes = output.writeToZip();
                    if (bytes == null) {
                        LOG.debug("Generated NULL bytes");

                    } else {
                        LOG.debug("Generated " + bytes.length + " bytes");

                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        UUID batchId = UUID.randomUUID();
                        EnterpriseFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
                    }
                }
            }

            statement.close();
            subscriberTransformConnection.close();

            LOG.debug("Finished Populating Missing Orgs In CompassV1 " + subscriberConfigName);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void populateMissingOrgsInCompassV2(String subscriberConfigName, boolean testMode) {
        LOG.debug("Populating Missing Orgs In CompassV2 " + subscriberConfigName + " testMode = " + testMode);
        try {

            Connection subscriberTransformConnection = ConnectionManager.getSubscriberTransformNonPooledConnection(subscriberConfigName);

            //get orgs in that subscriber DB
            Map<UUID, Long> hmOrgs = new HashMap<>();

            String sql = "SELECT service_id, enterprise_id FROM enterprise_organisation_id_map";
            Statement statement = subscriberTransformConnection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String s = rs.getString(1);
                long id = rs.getLong(2);
                hmOrgs.put(UUID.fromString(s), new Long(id));
            }
            LOG.debug("Found " + hmOrgs.size() + " orgs");

            for (UUID serviceId: hmOrgs.keySet()) {
                Long subscriberId = hmOrgs.get(serviceId);
                LOG.debug("Doing service " + serviceId + ", subscriber ID " + subscriberId);

                /*ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getById(serviceId);*/

                //find the FHIR Organization this is from
                sql = "SELECT source_id FROM subscriber_id_map WHERE subscriber_id = " + subscriberId;
                rs = statement.executeQuery(sql);
                if (!rs.next()) {
                    sql = "SELECT source_id FROM subscriber_id_map_3 WHERE subscriber_id = " + subscriberId;
                    rs = statement.executeQuery(sql);
                    if (!rs.next()) {
                        throw new Exception("Failed to find source ID for service ID " + serviceId + " and subscriber ID " + subscriberId);
                    }
                }

                String sourceId = rs.getString(1);
                Reference orgRef = ReferenceHelper.createReference(sourceId);

                //make sure our org is on the CoreXX server we expect and take over any instance mapping
                orgRef = findNewOrgRefOnCoreDb(subscriberConfigName, orgRef, serviceId, testMode);
                if (orgRef == null) {
                    LOG.warn("<<<<<<<<No Organization resource could be found for " + sourceId + ">>>>>>>>>>>>>>>>>>>>>>>>>");
                    continue;
                }

                //find the org and work up to find all its parents too
                List<ResourceWrapper> resourceWrappers = new ArrayList<>();

                while (orgRef != null) {

                    ReferenceComponents comps = ReferenceHelper.getReferenceComponents(orgRef);
                    if (comps.getResourceType() != ResourceType.Organization) {
                        throw new Exception("Found non-organisation resource mapping for subscriber ID " + subscriberId + ": " + sourceId);
                    }
                    UUID orgId = UUID.fromString(comps.getId());

                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Organization.toString(), orgId);
                    if (wrapper == null) {
                        throw new Exception("Failed to find resource wrapper for parent org with source ID " + sourceId);
                    }

                    resourceWrappers.add(wrapper);

                    Organization org = (Organization)wrapper.getResource();
                    if (org.hasPartOf()) {
                        orgRef = org.getPartOf();
                    } else {
                        orgRef = null;
                    }
                }

                if (testMode) {
                    LOG.debug("Found " + resourceWrappers.size() + " orgs");
                    for (ResourceWrapper wrapper: resourceWrappers) {
                        Organization org = (Organization)wrapper.getResource();
                        String odsCode = IdentifierHelper.findOdsCode(org);
                        LOG.debug("    Got " + org.getName() + " [" + odsCode + "]");
                    }

                } else {
                    SubscriberTransformHelper helper = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfigName, resourceWrappers, false);

                    OrganisationTransformer t = new OrganisationTransformer();
                    t.transformResources(resourceWrappers, helper);

                    OutputContainer output = helper.getOutputContainer();
                    byte[] bytes = output.writeToZip();
                    if (bytes == null) {
                        LOG.debug("Generated NULL bytes");

                    } else {
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        UUID batchId = UUID.randomUUID();
                        UUID queuedMessageId = UUID.randomUUID();
                        LOG.debug("Generated " + bytes.length + " bytes with batch ID " + batchId + " and queued message ID " + queuedMessageId);
                        SubscriberFiler.file(batchId, queuedMessageId, base64, subscriberConfigName);
                    }
                }
            }

            statement.close();
            subscriberTransformConnection.close();

            LOG.debug("Finished Populating Missing Orgs In CompassV2 " + subscriberConfigName);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }


    private static Reference findNewOrgRefOnCoreDb(String subscriberConfigName, Reference oldOrgRef, UUID serviceId, boolean testMode) throws Exception {

        try {
            String oldOrgId = ReferenceHelper.getReferenceId(oldOrgRef);

            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Organization.toString(), UUID.fromString(oldOrgId));
            if (wrapper != null) {
                LOG.trace("Organization exists at service");
                return oldOrgRef;
            }

            LOG.debug("Org doesn't exist at service, so need to take over instance mapping");

            Reference newOrgRef = null;

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false, 10000);
            for (UUID patientId : patientIds) {

                ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
                Patient patient = (Patient) resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientId.toString());
                if (patient == null) {
                    continue;
                }

                if (!patient.hasManagingOrganization()) {
                    throw new TransformException("Patient " + patient.getId() + " doesn't have a managing org for service " + serviceId);
                }

                newOrgRef = patient.getManagingOrganization();
                break;
            }

            if (newOrgRef == null) {
                throw new Exception("Failed to find new org ref from patient records");
            }

            String newOrgId = ReferenceHelper.getReferenceId(newOrgRef);

            if (newOrgId.equals(oldOrgId)) {
                LOG.debug("Org ref correct but doesn't exist on DB - reference data is missing???");
                return null;
            }

            if (testMode) {
                LOG.debug("Would need to take over instance mapping from " + oldOrgId + " -> " + newOrgId);

            } else {

                LOG.debug("Taking over instance mapping from " + oldOrgId + " -> " + newOrgId);

                //we need to update the subscriber transform DB to make this new org ref the defacto one
                SubscriberInstanceMappingDalI dal = DalProvider.factorySubscriberInstanceMappingDal(subscriberConfigName);
                dal.takeOverInstanceMapping(ResourceType.Organization, UUID.fromString(oldOrgId), UUID.fromString(newOrgId));

                LOG.debug("Done");
            }

            return newOrgRef;
        } catch (Exception e) {
            LOG.error("Exception finding org ref for service " + serviceId, e);
            return null;
        }
    }

    /*public static void testHashedFileFilteringForSRCode(String filePath, String uniqueKey) {
        LOG.info("Testing Hashed File Filtering for SRCode using " + filePath);
        try {


            //HashFunction hf = Hashing.md5();
            //HashFunction hf = Hashing.murmur3_128();
            HashFunction hf = Hashing.sha512();

            Hasher hasher = hf.newHasher();
            hasher.putString(filePath, Charset.defaultCharset());
            HashCode hc = hasher.hash();
            int fileUniqueId = hc.asInt();

            //copy file to local file
            String name = FilenameUtils.getName(filePath);
            String srcTempName = "TMP_SRC_" + name;
            String dstTempName = "TMP_DST_" + name;

            File srcFile = new File(srcTempName);
            File dstFile = new File(dstTempName);

            InputStream is = FileHelper.readFileFromSharedStorage(filePath);
            Files.copy(is, srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            is.close();
            LOG.debug("Copied " + srcFile.length() + "byte file from S3");

            CSVParser parser = CSVParser.parse(srcFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
            Map<String, Integer> headers = parser.getHeaderMap();
            if (!headers.containsKey(uniqueKey)) {
                LOG.debug("Headers found: " + headers);
                throw new Exception("Couldn't find unique key " + uniqueKey);
            }

            String[] headerArray = CsvHelper.getHeaderMapAsArray(parser);
            int uniqueKeyIndex = headers.get(uniqueKey).intValue();

            Map<StringMemorySaver, StringMemorySaver> hmHashes = new ConcurrentHashMap<>();

            LOG.debug("Starting hash calculations");

            int done = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String uniqueVal = record.get(uniqueKey);

                Hasher hashser = hf.newHasher();

                int size = record.size();
                for (int i=0; i<size; i++) {
                    if (i == uniqueKeyIndex) {
                        continue;
                    }

                    String val = record.get(i);
                    hashser.putString(val, Charset.defaultCharset());
                }

                hc = hashser.hash();
                String hashString = hc.toString();

                if (hmHashes.containsKey(uniqueVal)) {
                    LOG.error("Duplicate unique value [" + uniqueVal + "]");
                }
                hmHashes.put(new StringMemorySaver(uniqueVal), new StringMemorySaver(hashString));

                done ++;
                if (done % 100000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            parser.close();

            LOG.debug("Finished hash calculations for " + hmHashes.size());

            //hit DB for each record
            int threadPoolSize = 10;
            ThreadPool threadPool = new ThreadPool(threadPoolSize, 1000, "Tester");
            Map<String, String> batch = new HashMap<>();

            for (StringMemorySaver uniqueVal: hmHashes.keySet()) {
                StringMemorySaver hash = hmHashes.get(uniqueVal);

                batch.put(uniqueVal.toString(), hash.toString());

                if (batch.size() > TransformConfig.instance().getResourceSaveBatchSize()) {
                    threadPool.submit(new FindHashForBatchCallable(fileUniqueId, batch, hmHashes));
                    batch = new HashMap<>();
                }
            }
            if (!batch.isEmpty()) {
                threadPool.submit(new FindHashForBatchCallable(fileUniqueId, batch, hmHashes));
                batch = new HashMap<>();
            }
            threadPool.waitUntilEmpty();
            LOG.debug("Finished looking for hashes on DB, filtering down to " + hmHashes.size());

            //filter file
            CSVFormat format = CSVFormat.DEFAULT.withHeader(headerArray);

            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, format);

            parser = CSVParser.parse(srcFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String uniqueVal = record.get(uniqueKey);
                if (hmHashes.containsKey(new StringMemorySaver(uniqueVal))) {
                    csvPrinter.printRecord(record);
                }
            }

            parser.close();
            csvPrinter.close();
            LOG.debug("Finished filtering file with " + hmHashes.size() + " records");

            //store hashes in DB
            batch = new HashMap<>();

            for (StringMemorySaver uniqueVal: hmHashes.keySet()) {
                StringMemorySaver hash = hmHashes.get(uniqueVal);

                batch.put(uniqueVal.toString(), hash.toString());

                if (batch.size() > TransformConfig.instance().getResourceSaveBatchSize()) {
                    threadPool.submit(new SaveHashForBatchCallable(fileUniqueId, batch));
                    batch = new HashMap<>();
                }
            }
            if (!batch.isEmpty()) {
                threadPool.submit(new SaveHashForBatchCallable(fileUniqueId, batch));
                batch = new HashMap<>();
            }
            threadPool.waitUntilEmpty();
            LOG.debug("Finished saving hashes to DB");

            //delete all files
            srcFile.delete();
            dstFile.delete();

            LOG.info("Finished Testing Hashed File Filtering for SRCode using " + filePath);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    static class SaveHashForBatchCallable implements Callable {

        private int fileUniqueId;
        private Map<String, String> batch;

        public SaveHashForBatchCallable(int fileUniqueId, Map<String, String> batch) {
            this.fileUniqueId = fileUniqueId;
            this.batch = batch;
        }

        @Override
        public Object call() throws Exception {

            try {
                Connection connection = ConnectionManager.getEdsConnection();

                String sql = "INSERT INTO tmp.file_record_hash (file_id, record_id, record_hash) VALUES (?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE"
                        + " record_hash = VALUES(record_hash)";
                PreparedStatement ps = connection.prepareStatement(sql);

                for (String uniqueId: batch.keySet()) {
                    String hash = batch.get(uniqueId);

                    int col = 1;
                    ps.setInt(col++, fileUniqueId);
                    ps.setString(col++, uniqueId);
                    ps.setString(col++, hash);
                    ps.addBatch();
                }

                ps.executeBatch();
                connection.commit();

                ps.close();
                connection.close();

            } catch (Throwable t) {
                LOG.error("", t);
            }

            return null;
        }
    }

    static class FindHashForBatchCallable implements Callable {

        private int fileUniqueId;
        private Map<String, String> batch;
        private Map<StringMemorySaver, StringMemorySaver> hmHashs;

        public FindHashForBatchCallable(int fileUniqueId, Map<String, String> batch, Map<StringMemorySaver, StringMemorySaver> hmHashs) {
            this.fileUniqueId = fileUniqueId;
            this.batch = batch;
            this.hmHashs = hmHashs;
        }


        @Override
        public Object call() throws Exception {
            try {
                Connection connection = ConnectionManager.getEdsConnection();

                String sql = "SELECT record_id, record_hash FROM tmp.file_record_hash"
                        + " WHERE file_id = ? AND record_id IN (";
                for (int i=0; i<batch.size(); i++) {
                    if (i > 0) {
                        sql += ", ";
                    }
                    sql += "?";
                }
                sql += ")";
                PreparedStatement ps = connection.prepareStatement(sql);

                int col = 1;
                ps.setInt(col++, fileUniqueId);
                for (String uniqueId: batch.keySet()) {
                    ps.setString(col++, uniqueId);
                }

                ResultSet rs = ps.executeQuery();

                Map<String, String> hmResults = new HashMap<>();
                while (rs.next()) {
                    String recordId = rs.getString(1);
                    String hash = rs.getString(2);
                    hmResults.put(recordId, hash);
                }

                ps.close();
                connection.close();

                for (String uniqueId: batch.keySet()) {
                    String newHash = batch.get(uniqueId);
                    String dbHash = hmResults.get(uniqueId);

                    if (dbHash != null
                            && dbHash.equals(newHash)) {
                        hmHashs.remove(new StringMemorySaver(uniqueId));
                    }
                }

            } catch (Throwable t) {
                LOG.error("", t);
            }

            return null;
        }
    }*/

    public static void testHashedFileFilteringForSRCode(String filePath, String uniqueKey, String dataDateStr) {
        LOG.info("Testing Hashed File Filtering for SRCode using " + filePath);
        try {

            Date dataDate = new SimpleDateFormat("YYYY-MM-DD").parse(dataDateStr);

            //HashFunction hf = Hashing.md5();
            //HashFunction hf = Hashing.murmur3_128();
            HashFunction hf = Hashing.sha512();

            //copy file to local file
            String name = FilenameUtils.getName(filePath);
            String srcTempName = "TMP_SRC_" + name;
            String dstTempName = "TMP_DST_" + name;

            File srcFile = new File(srcTempName);
            File dstFile = new File(dstTempName);

            InputStream is = FileHelper.readFileFromSharedStorage(filePath);
            Files.copy(is, srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            is.close();
            LOG.debug("Copied " + srcFile.length() + " byte file from S3");

            long msStart = System.currentTimeMillis();

            CSVParser parser = CSVParser.parse(srcFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
            Map<String, Integer> headers = parser.getHeaderMap();
            if (!headers.containsKey(uniqueKey)) {
                LOG.debug("Headers found: " + headers);
                throw new Exception("Couldn't find unique key " + uniqueKey);
            }

            String[] headerArray = CsvHelper.getHeaderMapAsArray(parser);
            int uniqueKeyIndex = headers.get(uniqueKey).intValue();

            LOG.debug("Starting hash calculations");

            String hashTempName = "TMP_HSH_" + name;
            File hashFile = new File(hashTempName);
            FileOutputStream fos = new FileOutputStream(hashFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader("record_id", "record_hash"));

            int done = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String uniqueVal = record.get(uniqueKey);

                Hasher hashser = hf.newHasher();

                int size = record.size();
                for (int i=0; i<size; i++) {
                    if (i == uniqueKeyIndex) {
                        continue;
                    }

                    String val = record.get(i);
                    hashser.putString(val, Charset.defaultCharset());
                }

                HashCode hc = hashser.hash();
                String hashString = hc.toString();

                csvPrinter.printRecord(uniqueVal, hashString);

                done ++;
                if (done % 200000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            csvPrinter.close();
            parser.close();

            LOG.debug("Finished hash calculations for " + done + " records to " + hashFile);

            Set<StringMemorySaver> hsUniqueIdsToKeep = new HashSet<>();

            String tempTableName = ConnectionManager.generateTempTableName(FilenameUtils.getBaseName(filePath));


            //load into TEMP table
            Connection connection = ConnectionManager.getEdsNonPooledConnection();
            try {
                //turn on auto commit so we don't need to separately commit these large SQL operations
                connection.setAutoCommit(true);

                //create a temporary table to load the data into
                LOG.debug("Loading " + hashFile + " into " + tempTableName);
                String sql = "CREATE TABLE " + tempTableName + " ("
                        + "record_id varchar(255), "
                        + "record_hash char(128), "
                        + "record_exists boolean DEFAULT FALSE, "
                        + "ignore_record boolean DEFAULT FALSE, "
                        + "PRIMARY KEY (record_id))";
                Statement statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();

                //bulk load temp table, adding record number as we go
                LOG.debug("Starting bulk load into " + tempTableName);
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                sql = "LOAD DATA LOCAL INFILE '" + hashFile.getAbsolutePath().replace("\\", "\\\\") + "'"
                        + " INTO TABLE " + tempTableName
                        + " FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\\"'"
                        + " LINES TERMINATED BY '\\r\\n'"
                        + " IGNORE 1 LINES";
                statement.executeUpdate(sql);
                statement.close();

                //work out which records already exist in the target table
                LOG.debug("Finding records that exist in file_record_hash");
                sql = "UPDATE " + tempTableName + " s"
                        + " INNER JOIN tmp.file_record_hash t"
                        + " ON t.record_id = s.record_id"
                        + " SET s.record_exists = true,"
                        + " s.ignore_record = IF (s.record_hash = t.record_hash OR t.dt_last_updated > " + ConnectionManager.formatDateString(dataDate, true) + ", true, false)";
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();

                LOG.debug("Creating index on temp table");
                sql = "CREATE INDEX ix ON " + tempTableName + " (ignore_record)";
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();

                LOG.debug("Selecting IDs with different hashes");
                sql = "SELECT record_id FROM " + tempTableName + " s"
                        + " WHERE ignore_record = false";
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.setFetchSize(10000);
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    String id = rs.getString(1);
                    hsUniqueIdsToKeep.add(new StringMemorySaver(id));
                }

            } finally {
                //MUST change this back to false
                connection.setAutoCommit(false);
                connection.close();
            }
            LOG.debug("Found " + hsUniqueIdsToKeep.size() + " records to retain");


            //filter file
            CSVFormat format = CSVFormat.DEFAULT.withHeader(headerArray);

            fos = new FileOutputStream(dstFile);
            osw = new OutputStreamWriter(fos);
            bufferedWriter = new BufferedWriter(osw);
            csvPrinter = new CSVPrinter(bufferedWriter, format);

            parser = CSVParser.parse(srcFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String uniqueVal = record.get(uniqueKey);
                if (hsUniqueIdsToKeep.contains(new StringMemorySaver(uniqueVal))) {
                    csvPrinter.printRecord(record);
                }
            }

            parser.close();
            csvPrinter.close();
            LOG.debug("Finished filtering file with " + hsUniqueIdsToKeep.size() + " records");

            connection = ConnectionManager.getEdsNonPooledConnection();
            try {
                //turn on auto commit so we don't need to separately commit these large SQL operations
                connection.setAutoCommit(true);

                //update any records that previously existed, but have a changed term
                LOG.debug("Updating existing records in target table file_record_hash");
                String sql = "UPDATE tmp.file_record_hash t"
                        + " INNER JOIN " + tempTableName + " s"
                        + " ON t.record_id = s.record_id"
                        + " SET t.record_hash = s.record_hash,"
                        + " t.dt_last_updated = " + ConnectionManager.formatDateString(dataDate, true)
                        + " WHERE s.record_exists = true";
                Statement statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();

                //insert records into the target table where the staging has new records
                LOG.debug("Inserting new records in target table file_record_hash");
                sql = "INSERT IGNORE INTO tmp.file_record_hash (record_id, record_hash, dt_last_updated)"
                        + " SELECT record_id, record_hash, " + ConnectionManager.formatDateString(dataDate, true)
                        + " FROM " + tempTableName
                        + " WHERE record_exists = false";
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();

                //delete the temp table
                LOG.debug("Deleting temp table");
                sql = "DROP TABLE " + tempTableName;
                statement = connection.createStatement(); //one-off SQL due to table name, so don't use prepared statement
                statement.executeUpdate(sql);
                statement.close();


            } finally {
                //MUST change this back to false
                connection.setAutoCommit(false);
                connection.close();
            }

            LOG.debug("Finished saving hashes to DB");

            long msEnd = System.currentTimeMillis();
            LOG.debug("Took " + ((msEnd - msStart) / 1000) + " s");

            //delete all files
            srcFile.delete();
            hashFile.delete();
            dstFile.delete();

            LOG.info("Finished Testing Hashed File Filtering for SRCode using " + filePath);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void deleteResourcesForDeletedPatients(boolean testMode, String odsCodeRegex) {
        LOG.debug("Deleting Resources for Deleted Patients using " + odsCodeRegex);
        try {

            Connection conn = ConnectionManager.getEdsNonPooledConnection();

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                //only do publishers
                if (Strings.isNullOrEmpty(service.getPublisherConfigName())) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.isEmpty()) {
                    continue;
                }
                UUID systemId = systemIds.get(0);


                UUID exchangeId = UUID.randomUUID();
                List<UUID> batchIdsCreated = new ArrayList<>();
                String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                String odsCode = service.getLocalId();

                FhirResourceFiler filer = null;
                Exchange exchange = null;

                List<UUID> patientIds = new ArrayList<>();
                String sql = "SELECT patient_id FROM patient_search WHERE service_id = ? AND dt_deleted IS NOT NULL";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, service.getId().toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString(1);
                    patientIds.add(UUID.fromString(id));
                }
                ps.close();

                if (patientIds.isEmpty()) {
                    LOG.debug("No deleted patients found");
                    continue;
                }

                LOG.debug("Found " + patientIds.size() + " deleted patients");

                for (UUID patientId: patientIds) {

                    List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(service.getId(), patientId);
                    LOG.debug("Doing patient " + patientId + " with " + wrappers.size() + " resources");

                    for (ResourceWrapper wrapper: wrappers) {

                        if (testMode) {
                            LOG.debug("Would delete resource " + wrapper.getResourceType() + " " + wrapper.getResourceId());

                        } else {

                            if (exchange == null) {
                                filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                                exchange = new Exchange();
                                exchange.setId(exchangeId);
                                exchange.setBody(bodyJson);
                                exchange.setTimestamp(new Date());
                                exchange.setHeaders(new HashMap<>());
                                exchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
                                exchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
                                exchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
                                exchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
                                exchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.TPP_CSV);
                                exchange.setServiceId(service.getId());
                                exchange.setSystemId(systemId);

                                AuditWriter.writeExchange(exchange);
                                AuditWriter.writeExchangeEvent(exchange, "Manually created to delete resources for deleted patients");
                            }

                            //delete resource
                            Resource resource = wrapper.getResource();
                            filer.deletePatientResource(null, false, new GenericBuilder(resource));
                        }
                    }
                }

                if (!testMode) {

                    if (exchange != null) {

                        //close down filer
                        filer.waitToFinish();

                        //set multicast header
                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                        exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                        //post to Rabbit protocol queue
                        List<UUID> exchangeIds = new ArrayList<>();
                        exchangeIds.add(exchange.getId());
                        QueueHelper.postToExchange(exchangeIds, "EdsProtocol", null, true, null);

                        //set this after posting to rabbit so we can't re-queue it later
                        exchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                        AuditWriter.writeExchange(exchange);
                    }
                }
            }

            conn.close();

            LOG.debug("Finished Deleting Resources for Deleted Patients using " + odsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * checks to make sure that every exchange has been properly through the inbound transform without
     * being skipped, failing or being filtered on specific files
     */
    public static void findTppServicesNeedReprocessing(String odsCodeRegex) {
        LOG.debug("Finding TPP Services that Need Re-processing for " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("Wrong number of system IDs for " + service);
                }
                UUID systemId = systemIds.get(0);

                String publisherStatus = null;
                for (ServiceInterfaceEndpoint serviceInterface: service.getEndpointsList()) {
                    if (serviceInterface.getSystemUuid().equals(systemId)) {
                        publisherStatus = serviceInterface.getEndpoint();
                    }
                }

                if (publisherStatus == null) {
                    throw new Exception("Failed to find publisher status for service " + service);
                }

                LOG.debug("");
                LOG.debug("CHECKING " + service + " " + publisherStatus + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                if (publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL)) {
                    LOG.debug("Skipping service because set to auto-fail");
                    continue;
                }

                //check if in the bulks skipped table and are waiting to be re-queued
                Connection connection = ConnectionManager.getEdsConnection();
                String sql = "select 1 from audit.tpp_skipped_srcode where service_id = ? and (queued = false or queued is null)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, service.getId().toString());
                ResultSet rs = ps.executeQuery();
                boolean needsRequeueing = rs.next();
                ps.close();
                connection.close();
                if (needsRequeueing) {
                    LOG.debug(">>>>> NEEDS REQUEUEING FOR SKIPPED SRCode BULK");
                }

                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                List<Exchange> exchanges = exchangeDal.getExchangesByService(service.getId(), systemId, Integer.MAX_VALUE);
                for (int i=exchanges.size()-1; i>=0; i--) {
                    Exchange exchange = exchanges.get(i);

                    //if can't be queued, ignore it
                    Boolean allowQueueing = exchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
                    if (allowQueueing != null
                            && !allowQueueing.booleanValue()) {
                        continue;
                    }

                    //LOG.debug("Doing exchange " + exchange.getId() + " from " + exchange.getHeaderAsDate(HeaderKeys.DataDate));

                    List<ExchangeTransformAudit> audits = exchangeDal.getAllExchangeTransformAudits(service.getId(), systemId, exchange.getId());
                    List<ExchangeEvent> events = exchangeDal.getExchangeEvents(exchange.getId());

                    //was it transformed OK before it was re-queued with filtering?
                    boolean transformedWithoutFiltering = false;
                    List<String> logging = new ArrayList<>();

                    for (ExchangeTransformAudit audit: audits) {

                        //transformed OK
                        boolean transformedOk = audit.getEnded() != null && audit.getErrorXml() == null;
                        if (!transformedOk) {
                            logging.add("Audit " + audit.getStarted() + " didn't complete OK, so not counting");
                            continue;
                        }

                        //if transformed OK see whether filtering was applied BEFORE
                        Date dtTransformStart = audit.getStarted();
                        logging.add("Audit completed OK from " + audit.getStarted());

                        //find immediately proceeding event showing loading into inbound queue
                        ExchangeEvent previousLoadingEvent = null;
                        for (int j=events.size()-1; j>=0; j--) {
                            ExchangeEvent event = events.get(j);
                            Date dtEvent = event.getTimestamp();
                            if (dtEvent.after(dtTransformStart)) {
                                logging.add("Ignoring event from " + dtEvent + " as AFTER transform");
                                continue;
                            }

                            String eventDesc = event.getEventDesc();
                            if (eventDesc.startsWith("Manually pushed into edsInbound exchange")
                                    || eventDesc.startsWith("Manually pushed into EdsInbound exchange")) {
                                previousLoadingEvent = event;
                                logging.add("Proceeding event from " + dtEvent + " [" + eventDesc + "]");
                                break;
                            } else {
                                logging.add("Ignoring event from " + dtEvent + " as doesn't match text [" + eventDesc + "]");
                            }
                        }

                        if (previousLoadingEvent == null) {
                            //if transformed OK and no previous manual loading event, then it was OK
                            transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK without being manually loaded = OK");

                        } else {
                            //if transformed OK and was manually loaded into queue, then see if event applied filtering or not
                            String eventDesc = previousLoadingEvent.getEventDesc();
                            if (!eventDesc.contains("Filtered on file types")) {
                                transformedWithoutFiltering = true;
//LOG.debug("Audit from " + audit.getStarted() + " was transformed OK and was manually loaded without filtering = OK");

                            } else {
                                logging.add("Event desc filters on file types, so DIDN'T transform OK");
                            }
                        }
                    }

                    if (!transformedWithoutFiltering) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        LOG.error("" + service + " -> exchange " + exchange.getId() + " from " + sdf.format(exchange.getHeaderAsDate(HeaderKeys.DataDate)));
                        /*for (String line: logging) {
                            LOG.error("    " + line);
                        }*/
                    }
                }
            }

            LOG.debug("Finished Finding Emis Services that Need Re-processing");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void testSubscriberConfigs() {
        LOG.debug("Testing Subscriber Configs");
        try {

            Map<String, String> configs = ConfigManager.getConfigurations("db_subscriber");
            LOG.debug("Found " + configs.size() + " configs");

            for (String configId: configs.keySet()) {
                String configData = configs.get(configId);
                LOG.debug("Doing >>>>>>>>>>>>>>>> " + configId);
                LOG.debug(configData);

                try {
                    SubscriberConfig configRecord = SubscriberConfig.readFromConfig(configId);
                    LOG.debug("Parsed OK");
                    LOG.debug("" + configRecord);

                } catch (Exception ex) {
                    LOG.error("Failed to parse");
                    LOG.error("", ex);
                }
            }

            LOG.debug("Finished Testing Subscriber Configs");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    public static void validateProtocolCohorts() {
        LOG.debug("Validating Protocol Cohorts");
        try {

            DefinitionItemType itemType = DefinitionItemType.Protocol;

            Iterable<ActiveItem> activeItems = null;
            List<Item> items = new ArrayList();

            LibraryDalI repository = DalProvider.factoryLibraryDal();
            activeItems = repository.getActiveItemByTypeId(itemType.getValue(), false);

            for (ActiveItem activeItem: activeItems) {
                Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
                if (!item.isDeleted()) {
                    items.add(item);
                }
            }
            //LOG.trace("Found " + items.size() + " protocols to check for service " + serviceId + " and system " + systemId);

            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);

                String xml = item.getXmlContent();
                LibraryItem libraryItem = XmlSerializer.deserializeFromString(LibraryItem.class, xml, null);
                Protocol protocol = libraryItem.getProtocol();

                LOG.debug("");
                LOG.debug("");
                LOG.debug(">>>>>>>>>>>>>>>>> " + libraryItem.getName());

                try {

                    String cohort = protocol.getCohort();
                    if (Strings.isNullOrEmpty(cohort)) {
                        LOG.debug("Protocol doesn't have cohort explicitly set, so assuming ALL PATIENTS");

                    } else {

                        if (cohort.equals("All Patients")) {
                            LOG.debug("Cohort = all patients");

                        } else if (cohort.equals("Explicit Patients")) {
                            LOG.debug("Cohort = explicit patients");

                        } else if (cohort.startsWith("Defining Services")) {
                            LOG.debug("Cohort = defining services");

                            Set<String> odsCodes = RunDataDistributionProtocols.getOdsCodesForServiceDefinedProtocol(protocol);
                            LOG.debug("Cohort is " + odsCodes.size() + " size");

                            List<String> list = new ArrayList<>(odsCodes);
                            list.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));

                            Map<String, List<String>> hmParents = new HashMap<>();

                            for (String odsCode: list) {
                                OdsOrganisation org = OdsWebService.lookupOrganisationViaRest(odsCode);
                                if (org == null) {
                                    LOG.error(odsCode + " -> Failed to find ODS record");
                                } else {

                                    if (!org.isActive()) {
                                        LOG.error(odsCode + " -> ODS record not active");
                                    }

                                    Map<String, String> parents = org.getParents();
                                    for (String parentOdsCode: parents.keySet()) {

                                        List<String> l = hmParents.get(parentOdsCode);
                                        if (l == null) {
                                            l = new ArrayList<>();
                                            hmParents.put(parentOdsCode, l);
                                        }
                                        l.add(odsCode);
                                    }
                                }
                            }

                            LOG.debug("Found " + hmParents.size() + " parents");
                            for (String parentOdsCode: hmParents.keySet()) {
                                List<String> childOdsCodes = hmParents.get(parentOdsCode);

                                OdsOrganisation parentOrg = OdsWebService.lookupOrganisationViaRest(parentOdsCode);
                                if (parentOrg == null) {
                                    LOG.error(parentOdsCode + " -> Failed to find parent ODS record");
                                } else {
                                    LOG.error(parentOdsCode + " -> " + parentOrg.getOrganisationName());
                                }
                                LOG.debug("    Has " + childOdsCodes.size() + " children");
                                LOG.debug("    " + String.join(", " + childOdsCodes));
                            }
                        } else {
                            throw new Exception("Unknown cohort type [" + cohort + "]");
                        }
                    }

                } catch (Exception ex) {
                    LOG.error("", ex);
                }


            }


            LOG.debug("Finished Validating Protocol Cohorts");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void countVaccinationCodes(String sinceDateStr, String ccgOdsCodes) {
        LOG.debug("Counting VaccinationCodes at " + ccgOdsCodes);
        try {

            Date cutoff = new SimpleDateFormat("yyyy-MM-dd").parse(sinceDateStr);
            LOG.debug("Counting vaccinations since " + sinceDateStr);

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();


            Map<String, AtomicInteger> emisResults = new HashMap<>();
            Map<String, AtomicInteger> tppResults = new HashMap<>();
            Map<String, AtomicInteger> visionResults = new HashMap<>();


            for (Service service: services) {

                if (shouldSkipService(service, ccgOdsCodes)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                Map<String, AtomicInteger> hmResults = null;
                if (service.getTags() == null) {
                    LOG.warn("No tags set");
                    continue;
                } else if (service.getTags().containsKey("TPP")) {
                    hmResults = tppResults;
                } else if (service.getTags().containsKey("EMIS")) {
                    hmResults = emisResults;
                } else if (service.getTags().containsKey("Vision")) {
                    hmResults = visionResults;
                } else {
                    LOG.error("Unknown system type");
                    continue;
                    //throw new Exception();
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.YEAR, -20);
                Date d = cal.getTime();
                //String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(d);

                List<UUID> patientIds = new ArrayList<>();

                String sql = "SELECT patient_id FROM patient_search WHERE dt_deleted IS NULL AND date_of_birth > ? AND service_id = ?";
                Connection connection = ConnectionManager.getEdsConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setTimestamp(1, new java.sql.Timestamp(d.getTime()));
                ps.setString(2, service.getId().toString());

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String s = rs.getString(1);
                    patientIds.add(UUID.fromString(s));
                }

                ps.close();
                connection.close();

                LOG.debug("Found " + patientIds.size() + " patient IDs");

                int done = 0;
                for (UUID patientId: patientIds) {

                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    List<ResourceWrapper> resources = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Immunization.toString());
                    for (ResourceWrapper resourceWrapper: resources) {
                        Immunization imm = (Immunization)resourceWrapper.getResource();

                        if (!imm.hasDateElement()) {
                            continue;
                        }
                        DateTimeType dtVal = imm.getDateElement();
                        Date dt = dtVal.getValue();
                        if (dt.before(cutoff)) {
                            continue;
                        }

                        if (imm.hasVaccineCode()) {
                            CodeableConcept cc = imm.getVaccineCode();
                            ObservationCodeHelper codes = ObservationCodeHelper.extractCodeFields(cc);

                            Long snomedConceptId = codes.getSnomedConceptId();
                            String originalCode = codes.getOriginalCode();
                            String originalTerm = codes.getOriginalTerm();

                            //there are Vision immunizations with a Snomed code only
                            if (originalCode == null) {
                                originalCode = "NULL";
                            }

                            if (originalTerm == null) {
                                originalTerm = "NULL";
                            }

                            String snomedConceptIdStr;
                            if (snomedConceptId != null) {
                                snomedConceptIdStr = "" + snomedConceptId;
                            } else {
                                snomedConceptIdStr = "NULL";
                            }

                            String cacheKey = originalCode + "¬" + originalTerm + "¬" + snomedConceptIdStr;
                            AtomicInteger count = hmResults.get(cacheKey);
                            if (count == null) {
                                count = new AtomicInteger(0);
                                hmResults.put(cacheKey, count);
                            }
                            count.incrementAndGet();
                        }
                    }

                    done ++;
                    if (done % 100 == 0) {
                        LOG.debug("Done " + done);
                    }
                }
                LOG.debug("Finished " + done);
            }

            LOG.debug("Writing results");

            List<String> fileNames = new ArrayList<>();
            fileNames.add("Immunisation_Codes_TPP.csv");
            fileNames.add("Immunisation_Codes_Emis.csv");
            fileNames.add("Immunisation_Codes_Vision.csv");

            for (String fileName: fileNames) {

                Map<String, AtomicInteger> hmResults = null;
                String localScheme = null;
                if (fileName.equals("Immunisation_Codes_TPP.csv")) {
                    hmResults = tppResults;
                    localScheme = "TPP local";
                } else if (fileName.equals("Immunisation_Codes_Emis.csv")) {
                    hmResults = emisResults;
                    localScheme = "EMIS local";
                } else if (fileName.equals("Immunisation_Codes_Vision.csv")) {
                    hmResults = visionResults;
                    localScheme = "Vision local";
                } else {
                    throw new Exception("Unknown file name " + fileName);
                }

                //find max count
                Map<Integer, List<String>> hmByCount = new HashMap<>();
                int max = 0;

                for (String key: hmResults.keySet()) {
                    AtomicInteger a = hmResults.get(key);
                    int count = a.get();

                    List<String> l = hmByCount.get(new Integer(count));
                    if (l == null) {
                        l = new ArrayList<>();
                        hmByCount.put(new Integer(count), l);
                    }
                    l.add(key);

                    max = Math.max(max, count);
                }

                File dstFile = new File(fileName);
                FileOutputStream fos = new FileOutputStream(dstFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bufferedWriter = new BufferedWriter(osw);

                CSVFormat format = EmisCsvToFhirTransformer.CSV_FORMAT
                        .withHeader("Code Scheme", "Code", "Term", "Mapped Snomed Concept", "Mapped Snomed Term", "Count"
                        );
                CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

                for (int i=max; i>=0; i--) {
                    List<String> l = hmByCount.get(new Integer(i));
                    if (l == null) {
                        continue;
                    }

                    for (String key: l) {
                        String[] toks = key.split("¬");
                        String originalCode = toks[0];

                        String originalTerm = "NULL";
                        String snomedConceptId = "NULL";
                        String snomedTerm = "NULL";

                        if (toks.length > 1) {
                            originalTerm = toks[1];
                        }

                        if (toks.length > 2) {
                            snomedConceptId = toks[2];

                            SnomedCode snomedCode = TerminologyService.lookupSnomedFromConceptId(snomedConceptId);
                            if (snomedCode != null) {
                                snomedTerm = snomedCode.getTerm();
                            }
                        }

                        String codeScheme = null;

                        if (originalCode.startsWith("CTV3_")) {
                            originalCode = originalCode.substring(5);
                            if (originalCode.startsWith("Y")) {
                                codeScheme = localScheme;
                            } else {
                                codeScheme = "CTV3";
                            }

                        } else {
                            Read2Code dbCode = TerminologyService.lookupRead2Code(originalCode);
                            if (dbCode == null) {
                                codeScheme = localScheme;
                            } else {
                                codeScheme = "Read2";
                            }
                        }

                        printer.printRecord(codeScheme, originalCode, originalTerm, snomedConceptId, snomedTerm, new Integer(i));
                        //String cacheKey = originalCode + "|" + originalTerm + "|" + snomedConceptId;
                    }
                }

                printer.close();
            }

            LOG.debug("Finished Counting VaccinationCodes at " + ccgOdsCodes);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
