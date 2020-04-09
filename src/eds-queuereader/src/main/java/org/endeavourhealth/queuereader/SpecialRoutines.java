package org.endeavourhealth.queuereader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.FileInfo;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.application.ApplicationHeartbeatHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeBatch;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataSharingAgreementCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.messaging.pipeline.components.DetermineRelevantProtocolIds;
import org.endeavourhealth.im.client.IMClient;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.endeavourhealth.transform.common.TransformConfig;
import org.endeavourhealth.transform.subscriber.IMConstant;
import org.endeavourhealth.transform.subscriber.IMHelper;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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


                            /*String rootDir = null;
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
                            }*/

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

            String encounterScheme = IMConstant.DCE_Type_of_encounter;
            String encounterTerm = "Clinical";
            Integer encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.DCE_Type_of_encounter;
            encounterTerm = "Administrative";
            encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.DCE_Type_of_encounter;
            encounterTerm = "GP Surgery";
            encounterConceptId = IMClient.getConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);


            LOG.debug("----getMappedCoreConceptDbidForTypeTerm------------------------------------------------");
            LOG.debug("    gets CORE DBID for encounter type text");

            encounterScheme = IMConstant.DCE_Type_of_encounter;
            encounterTerm = "Clinical";
            encounterConceptId = IMClient.getMappedCoreConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.DCE_Type_of_encounter;
            encounterTerm = "Administrative";
            encounterConceptId = IMClient.getMappedCoreConceptDbidForTypeTerm(encounterScheme, encounterTerm);
            LOG.debug("For " + encounterScheme + " " + encounterTerm + " got " + encounterConceptId);

            encounterScheme = IMConstant.DCE_Type_of_encounter;
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

            LOG.debug("");
            LOG.debug("");


            LOG.info("Finished Testing DSM for " + odsCode);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void compareDsm(String filterOdsCode) {
        LOG.debug("Comparing DSM to DDS-UI for " + filterOdsCode);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                String odsCode = service.getLocalId();
                UUID serviceId = service.getId();

                //skip if filtering on ODS code
                if (!Strings.isNullOrEmpty(filterOdsCode)
                        && !odsCode.equalsIgnoreCase(filterOdsCode)) {
                    continue;
                }

                //only do if a publisher
                String publisherConfig = service.getPublisherConfigName();
                if (Strings.isNullOrEmpty(publisherConfig)) {
                    continue;
                }

                LOG.debug("Doing " + service + "--------------------------------------------------------------------");

                //check if publisher to DDS protocol
                List<String> protocolIdsOldWay = DetermineRelevantProtocolIds.getProtocolIdsForPublisherServiceOldWay(serviceId.toString());
                boolean hasDpaOldWay = !protocolIdsOldWay.isEmpty(); //in the old way, we count as having a DPA if they're in any protocol

                //check if DSM DPA
                boolean hasDpaNewWay = OrganisationCache.doesOrganisationHaveDPA(odsCode);

                if (hasDpaNewWay != hasDpaOldWay) {
                    LOG.error("Old and new DPA do not match (old = " + hasDpaOldWay + ", new = " + hasDpaNewWay + ")");
                    continue;
                }

                LOG.debug("Old and new DPA match (" + hasDpaOldWay + ")");

                //TODO - go on to check subscribers

                LOG.debug("");
            }

            LOG.debug("Finished Comparing DSM to DDS-UI for " + filterOdsCode);
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

    public static void testCallToDdsUi() {

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

            /*Response response = testTarget.
                    .request()
                    .header("Authorization", "Bearer " + kcClient.getToken().getToken())
                    .get();*/

            //request.Headers.Add("Authorization", this.token);

            int status = response.getStatus();
            LOG.debug("Status = " + status);
            String responseStr = response.readEntity(String.class);
            LOG.debug("responseStr = " + responseStr);

        } catch (Throwable t) {
            LOG.error("", t);
        }

    }
}
