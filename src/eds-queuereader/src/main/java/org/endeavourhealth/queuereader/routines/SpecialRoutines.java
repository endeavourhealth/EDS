package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientLinkPair;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberPersonMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.im.client.IMClient;
import org.endeavourhealth.im.models.mapping.MapColumnRequest;
import org.endeavourhealth.im.models.mapping.MapColumnValueRequest;
import org.endeavourhealth.im.models.mapping.MapResponse;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.enterprise.EnterpriseTransformHelper;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.enterprise.transforms.EpisodeOfCareEnterpriseTransformer;
import org.endeavourhealth.transform.enterprise.transforms.PatientEnterpriseTransformer;
import org.endeavourhealth.transform.subscriber.*;
import org.endeavourhealth.transform.subscriber.targetTables.OutputContainer;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.endeavourhealth.transform.subscriber.transforms.EpisodeOfCareTransformer;
import org.endeavourhealth.transform.subscriber.transforms.PatientTransformer;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SpecialRoutines extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialRoutines.class);
    public static final String COMPASS_V1 = "compass_v1";
    public static final String COMPASS_V2 = "compass_v2";

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


    /*public static void getJarDetails() {
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
    }*/

    /*public static void breakUpAdminBatches(String odsCodeRegex) {
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

                    //skip ADT
                    if (systemId.equals(UUID.fromString("d874c58c-91fd-41bb-993e-b1b8b22038b2"))) {
                        LOG.warn("Skipping ADT feed with system ID " + systemId);
                        continue;
                    }

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
    }*/

    public static void testInformationModelMapping() throws Exception{
        LOG.debug("Testing Information Model Mapping");

        Map<String, MapResponse> mappedColumnRequestResponseCache = new ConcurrentHashMap<>();
        Map<String, MapResponse> mappedColumnValueRequestResponseCache = new ConcurrentHashMap<>();

        //////// 1st instance definition
        MapColumnRequest propertyRequest1 = new MapColumnRequest(
                "CM_Org_Barts", "CM_Sys_Cerner", "CDS", "emergency",
                "attendance_category"
        );
        MapResponse propertyResponse1 = IMClient.getMapProperty(propertyRequest1);
        String propertyRequest1Key = propertyRequest1.getProvider()+":"
                                        +propertyRequest1.getSystem()+":"
                                        +propertyRequest1.getSchema()+":"
                                        +propertyRequest1.getTable()+":"
                                        +propertyRequest1.getColumn();
        mappedColumnRequestResponseCache.put(propertyRequest1Key, propertyResponse1);
        LOG.debug("First property response instance queried and added to cache using key: "+propertyRequest1Key);

        MapColumnValueRequest valueRequest1 = new MapColumnValueRequest(
                "CM_Org_Barts", "CM_Sys_Cerner", "CDS", "emergency",
                "attendance_category", "01", "CM_NHS_DD"
        );
        MapResponse valueResponse1 = IMClient.getMapPropertyValue(valueRequest1);
        String propertyValueRequest1Key = valueRequest1.getProvider()+":"
                                        +valueRequest1.getSystem()+":"
                                        +valueRequest1.getSchema()+":"
                                        +valueRequest1.getTable()+":"
                                        +valueRequest1.getColumn()+":"
                                        +valueRequest1.getValue().getCode()+":"
                                        +valueRequest1.getValue().getScheme();
        mappedColumnValueRequestResponseCache.put(propertyValueRequest1Key, valueResponse1);
        LOG.debug("First property value response instance queried and added to cache using key: "+propertyValueRequest1Key);

        ///////// 2nd instance (same definition as first)
        MapColumnRequest propertyRequest2 = new MapColumnRequest(
                "CM_Org_Barts", "CM_Sys_Cerner", "CDS", "emergency",
                "attendance_category"
        );
        String propertyRequest2Key = propertyRequest2.getProvider()+":"
                +propertyRequest2.getSystem()+":"
                +propertyRequest2.getSchema()+":"
                +propertyRequest2.getTable()+":"
                +propertyRequest2.getColumn();

        //is it in the cache using the same key construct as 1?
        MapResponse propertyResponse2 = mappedColumnRequestResponseCache.get(propertyRequest2Key);
        if (propertyResponse2 != null) {
            LOG.debug("Property response from 1st definition FOUND in cache using key: "+propertyRequest2Key);
        } else {
            LOG.debug("Property response from 1st definition NOT found in cache using key: "+propertyRequest2Key);
        }

        MapColumnValueRequest valueRequest2 = new MapColumnValueRequest(
                "CM_Org_Barts", "CM_Sys_Cerner", "CDS", "emergency",
                "attendance_category", "01", "CM_NHS_DD"
        );
        String propertyValueRequest2Key = valueRequest2.getProvider()+":"
                +valueRequest2.getSystem()+":"
                +valueRequest2.getSchema()+":"
                +valueRequest2.getTable()+":"
                +valueRequest2.getColumn()+":"
                +valueRequest2.getValue().getCode()+":"
                +valueRequest2.getValue().getScheme();
        //is it in the cache?
        MapResponse valueResponse2 = mappedColumnValueRequestResponseCache.get(propertyValueRequest2Key);
        if (valueResponse2 != null) {
            LOG.debug("Property value response from 1st definition FOUND in cache using key: "+propertyValueRequest2Key);
        } else {
            LOG.debug("Property value response from 1st definition NOT found in cache using key: "+propertyValueRequest2Key);
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


            LOG.debug("");
            LOG.debug("");
            LOG.debug("----EMIS Covid ------------------------------------------------");

            String emisScheme = IMConstant.EMIS_LOCAL;

            String emisCode = "^ESCT1299074";
            String emisTerm = "2019-nCoV (novel coronavirus) detected";

            Integer emisCoreConceptId = IMHelper.getIMMappedConcept(null, null, emisScheme, emisCode);
            Integer emisNonCoreConceptId = IMHelper.getIMConcept(null, null, emisScheme, emisCode, emisTerm);
            LOG.debug("    " + emisCode + " -> non-core " + emisNonCoreConceptId + " -> core " + emisCoreConceptId);

            emisCode = "^ESCT1299077";
            emisTerm = "2019-nCoV (novel coronavirus) not detected";

            emisCoreConceptId = IMHelper.getIMMappedConcept(null, null, emisScheme, emisCode);
            emisNonCoreConceptId = IMHelper.getIMConcept(null, null, emisScheme, emisCode, emisTerm);
            LOG.debug("    " + emisCode + " -> non-core " + emisNonCoreConceptId + " -> core " + emisCoreConceptId);


            LOG.debug("");
            LOG.debug("");
            LOG.debug("----TPP Covid ------------------------------------------------");

            String tppScheme = IMConstant.TPP_LOCAL;

            String tppCode = "Y20d1";
            String tppTerm = "Confirmed 2019-nCoV (Wuhan) infection";

            Integer tppCoreConceptId = IMHelper.getIMMappedConcept(null, null, tppScheme, tppCode);
            Integer tppNonCoreConceptId = IMHelper.getIMConcept(null, null, tppScheme, tppCode, tppTerm);
            LOG.debug("    " + tppCode + " -> non-core " + tppNonCoreConceptId + " -> core " + tppCoreConceptId);

            tppCode = "Y20d2";
            tppTerm = "Excluded 2019-nCoV (Wuhan) infection";

            tppCoreConceptId = IMHelper.getIMMappedConcept(null, null, tppScheme, tppCode);
            tppNonCoreConceptId = IMHelper.getIMConcept(null, null, tppScheme, tppCode, tppTerm);
            LOG.debug("    " + tppCode + " -> non-core " + tppNonCoreConceptId + " -> core " + tppCoreConceptId);


            LOG.debug("");
            LOG.debug("");
            LOG.debug("----SD-308 Free-text Referral Priorities ------------------------------------------------");

            List<String> toks = new ArrayList<>();
            toks.add("0 - 48 hours");
            toks.add("Choose and Book");
            toks.add("Planned");
            toks.add("Priority 4: 8 weeks");
            toks.add("0 - 1 Week");
            toks.add("25 Days");
            toks.add("Low Need");
            toks.add("Next Month (please specify)");
            toks.add("Priority");
            toks.add("Same Day");
            toks.add("High Risk");
            toks.add("Within 5 days");
            toks.add("72 Hours");
            toks.add("Priority 1: 2 working days");
            toks.add("Triage");
            toks.add("Low Risk");
            toks.add("Priority 2: 10 working days");
            toks.add("Not in Use");
            toks.add("Priority 3: 4 weeks");
            toks.add("0 - 10 DAYS");
            toks.add("Advice");
            toks.add("0 - 2 hours");
            toks.add("Planned (4 weeks)");
            toks.add("Medium");
            toks.add("2 - 48 Hours");

            for (String tok: toks) {
                Integer conceptId = IMClient.getConceptDbidForTypeTerm(IMConstant.FHIR_REFERRAL_PRIORITY, tok, true);
                LOG.debug("" + tok + " -> " + conceptId);
            }


            LOG.debug("----SD-308 Free-text Referral Types ------------------------------------------------");

            toks = new ArrayList<>();
            toks.add("Admission");
            toks.add("Bereavement support");
            toks.add("Discharge planning");
            toks.add("Information to patient/carers");
            toks.add("Pain / Symptom Control");
            toks.add("Social/financial");
            toks.add("Emotional/psychological support");
            toks.add("Assessment for hospice");
            toks.add("Other (Specify)");
            toks.add("DN - Falls Assessment");
            toks.add("Cancers");
            toks.add("Anaemia");

            for (String tok: toks) {
                Integer conceptId = IMClient.getConceptDbidForTypeTerm(IMConstant.FHIR_REFERRAL_TYPE, tok, true);
                LOG.debug("" + tok + " -> " + conceptId);
            }

            LOG.debug("Finished Testing Information Model");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }

    /*public static void testDsm(String odsCode) {
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
    }*/

    /*public static void compareDsmPublishers(boolean logDifferencesOnly, String odsCodeRegex) {
        LOG.debug("Comparing DSM to DDS-UI for " + odsCodeRegex);
        LOG.debug("logDifferencesOnly = " + logDifferencesOnly);
        try {

            File dstFile = new File("Publisher_DSM_Comparison.csv");
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
                boolean hasDpaOldWay = PublisherHelper.hasDpaUsingOldProtocols(serviceId, odsCode);
                boolean hasDpaNewWay = PublisherHelper.hasDpaUsingDsm(odsCode);
                boolean dpaMatches = hasDpaNewWay == hasDpaOldWay;

                if (!dpaMatches) {
                    logging.add("Old and new DPA do not match (old = " + hasDpaOldWay + ", new = " + hasDpaNewWay + ")");
                    gotDifference = true;

                } else {

                    logging.add("Old and new DPA match (" + hasDpaOldWay + ")");
                }

                //check DSAs
                List<String> subscriberConfigNamesOldWay = SubscriberHelper.getSubscriberConfigNamesFromOldProtocols(serviceId, odsCode);
                List<String> subscriberConfigNamesNewWay = SubscriberHelper.getSubscriberConfigNamesFromDsm(serviceId, odsCode);
                boolean dsaMatches = subscriberConfigNamesOldWay.equals(subscriberConfigNamesNewWay);

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

                printer.printRecord(service.getName(), odsCode, service.getCcgCode(), notesStr, hasDpaOldWay, hasDpaNewWay, dpaMatches, subscriberConfigNamesOldWay, subscriberConfigNamesNewWay, dsaMatches);

                logging.add("");

                //log what we found if we need to
                if (!logDifferencesOnly || gotDifference) {
                    for (String line: logging) {
                        LOG.debug(line);
                    }
                }
            }

            printer.close();

            LOG.debug("Finished Comparing DSM to DDS-UI for " + odsCodeRegex + " to " + dstFile);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/


    /*public static void testBulkLoad(String s3Path, String tableName) {
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
    }*/

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


    /*public static void catptureBartsEncounters(int count, String toFile) {
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

    }*/

    /*public static void transformAdtEncounters(String odsCode, String tableName) {
        LOG.debug("Transforming " + odsCode + " Encounters from " + tableName);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getByLocalIdentifier(odsCode);
            LOG.debug("Running for " + service);

            *//*SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date from = sdf.parse(dFrom);
            Date to = sdf.parse(dTo);*//*

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
                                        PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig(QueueHelper.ExchangeName.PROTOCOL);
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
    }*/


	// For the config name provided, get the list of services which are publishers and send
    // their transformed Patient and EpisodeOfCare FHIR resources to the Filer
    public static void transformAndFilePatientsAndEpisodesForProtocolServices(String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.debug("Populating Compass Patient and Registration Status History Table for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "bulk load of patient data and registration_status_history for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                //check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int batchSize = 0;
                org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    //check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    //retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        //throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        PatientLinkPair pair = patientLinkDal.updatePersonId(serviceId, fhirPatient);
                    }

                    //transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {

                        if (compassV1Container == null) {
                            compassV1Container = new org.endeavourhealth.transform.enterprise.outputModels.OutputContainer(subscriberConfig.isPseudonymised());
                        }

                        Long enterprisePatientId = patientIdDal.findEnterpriseIdOldWay(patientWrapper.getResourceType(), patientWrapper.getResourceId().toString());
                        if (enterprisePatientId == null) {
                            throw new Exception("Failed to find enterprisePatientId for patient " + patientWrapper.getResourceId());
                        }

                        EnterpriseTransformHelper params = new EnterpriseTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV1Container);
                        Long orgId = FhirToEnterpriseCsvTransformer.findEnterpriseOrgId(serviceId, params);
                        params.setEnterpriseOrganisationId(orgId);

                        PatientEnterpriseTransformer t = new PatientEnterpriseTransformer();
                        org.endeavourhealth.transform.enterprise.outputModels.Patient writer = params.getOutputContainer().getPatients();
                        t.transformResources(wrappers, writer, params);

                        List<ResourceWrapper> episodeWrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.EpisodeOfCare.toString());
                        org.endeavourhealth.transform.enterprise.outputModels.EpisodeOfCare episodeWriter = params.getOutputContainer().getEpisodesOfCare();
                        params.populatePatientAndPersonIds();
                        EpisodeOfCareEnterpriseTransformer eoc = new EpisodeOfCareEnterpriseTransformer();
                        eoc.transformResources(episodeWrappers, episodeWriter, params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV1PatientData(subscriberConfigName, compassV1Container);
                            compassV1Container = null;
                            batchSize = 0;
                        }

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {

                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV2Container);
                        Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                        params.setSubscriberOrganisationId(orgId);
                        PatientTransformer t = new PatientTransformer();
                        t.transformResources(wrappers, params);

                        List<ResourceWrapper> episodeWrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.EpisodeOfCare.toString());
                        params.populatePatientAndPersonIds();
                        EpisodeOfCareTransformer eoc = new EpisodeOfCareTransformer();
                        eoc.transformResources(episodeWrappers, params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV2PatientData(subscriberConfigName, compassV2Container);
                            compassV2Container = null;
                            batchSize = 0;
                        }

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //save any part-completed batch
                if (batchSize > 0) {

                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
                        saveCompassV1PatientData(subscriberConfigName, compassV1Container);

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2PatientData(subscriberConfigName, compassV2Container);

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Populating Compass Patient Pseudo ID Table for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    // For the config name provided, get the list of services which are publishers
    // and send their transformed Patient FHIR resources to the Filer
    public static void transformAndFilePatientAddressV2DataForProtocolServices(String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.debug("Populating CompassV2 patient, pat_address, pat_address_match and pat_address_ralf tables for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "Bulk load of patient, pat_address, pat_address_match and pat_address_ralf tables for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                // check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int batchSize = 0;
                // org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    // check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    // retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        // throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        PatientLinkPair pair = patientLinkDal.updatePersonId(serviceId, fhirPatient);
                    }

                    // transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {

                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV2Container);
                        Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                        params.setSubscriberOrganisationId(orgId);
                        PatientTransformer pt = new PatientTransformer();
                        pt.transformResources(wrappers, params);

                        // if batch is full then save what has been done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV2PatientAddressData(subscriberConfigName, compassV2Container);
                            compassV2Container = null;
                            batchSize = 0;
                        }
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //save any part completed batch
                if (batchSize > 0) {

                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2PatientAddressData(subscriberConfigName, compassV2Container);
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //audit what has been done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished populating CompassV2 patient, pat_address, pat_address_match and pat_address_ralf tables for " + subscriberConfigName + " regex " + orgOdsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    // For the config name provided, get the list of services which are publishers
    // and send their transformed Patient FHIR resources to the Filer
    public static void transformAndFilePatientAgeV2DataForProtocolServices(boolean includeStartedButNotFinishedServices, String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.debug("Populating CompassV2 patient (and person) tables for additional age columns for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "Bulk load of patient (and person) tables for additional age columns for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                if (includeStartedButNotFinishedServices) {
                    //check if already done, so we can make sure EVERY service is done
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }

                } else {
                    //check if already started, to allow us to run multiple instances of this at once
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already started or done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int done = 0;
                int batchSize = 0;
                // org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    // check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    // retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        // throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        LOG.warn("Null or empty person ID for " + fhirPatient.getId());
                        continue;
                        // throw new Exception("Null or empty person ID for " + fhirPatient.getId());
                    }

                    // transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            LOG.warn("Failed to find subscriberPatientId for " + ref);
                            continue;
                            // throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        try {
                            SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV2Container);
                            Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                            params.setSubscriberOrganisationId(orgId);
                            PatientTransformer pt = new PatientTransformer();
                            pt.transformResources(wrappers, params);
                            // if batch is full then save what has been done
                            batchSize++;
                            if (batchSize >= 100) {
                                saveCompassV2PatientAgeData(subscriberConfigName, compassV2Container);
                                compassV2Container = null;
                                batchSize = 0;
                            }
                        } catch (Throwable t) {
                            LOG.warn("Error with transform for patient " + patientId, t);
                        }
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }

                    done ++;
                    if (done % 1000 == 0) {
                        LOG.debug("Done " + done + " / " + patientIds.size() + " patients");
                    }
                }

                //save any part completed batch
                if (batchSize > 0) {
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2PatientAgeData(subscriberConfigName, compassV2Container);
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                LOG.debug("Completed " + done + " / " + patientIds.size() + " patients");

                //audit what has been done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished populating CompassV2 patient (and person) tables for additional age columns for " + subscriberConfigName + " regex " + orgOdsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * we've found that the TPP data contains registration data for other organisations, which
     * is causing a lot of confusion (and potential duplication). The transform now doesn't process these
     * records, and this routine will tidy up any existing data
     */
    /*public static void deleteTppEpisodesElsewhere(String odsCodeRegex, boolean testMode) {
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
    }*/

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
                            QueueHelper.postToExchange(exchangeIdBatch, QueueHelper.ExchangeName.INBOUND, null, reason);
                            exchangeIdBatch = new ArrayList<>();
                            LOG.info("Done " + count);
                        }
                    }

                    if (!exchangeIdBatch.isEmpty()) {
                        QueueHelper.postToExchange(exchangeIdBatch, QueueHelper.ExchangeName.INBOUND, null, reason);
                        LOG.info("Done " + count);
                    }
                }
            }

            LOG.info("Finished Posting to inbound");
        } catch (Throwable ex) {
            LOG.error("", ex);
        }
    }

    /**
     * routine to tidy up the mess left by moving part-transformed practices from Core06 to 07 and 08
     */
    /*public static void deleteCore06DataFromSubscribers(boolean testMode, String sourceSubscriberConfigName, String tableOfPatientIds, String tableForAudit, List<String> subscriberNames) {
        LOG.debug("Deleting Core06 Data From Subscribers Using " + tableOfPatientIds + " from " + String.join(", ", subscriberNames));
        try {
            Set<Long> patientIds = new HashSet<>();

            Connection connection = ConnectionManager.getSubscriberNonPooledConnection(sourceSubscriberConfigName);
            String sql = "SELECT patient_id FROM " + tableOfPatientIds + " WHERE done = false";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                long patientId = rs.getLong(1);
                patientIds.add(new Long(patientId));
            }
            LOG.debug("Found " + patientIds.size() + " patient IDs");

            List<String> tablesToDo = new ArrayList<>();
            //tablesToDo.add("patient"); //do separately
            tablesToDo.add("episode_of_care");
            tablesToDo.add("appointment");
            tablesToDo.add("encounter");
            tablesToDo.add("allergy_intolerance");
            tablesToDo.add("medication_statement");
            tablesToDo.add("medication_order");
            tablesToDo.add("flag");
            tablesToDo.add("observation");
            tablesToDo.add("diagnostic_order");
            tablesToDo.add("procedure_request");
            tablesToDo.add("referral_request");
            tablesToDo.add("patient_contact");
            tablesToDo.add("patient_address");
            //all other tables with a patient_id are empty

            int done = 0;
            for (Long patientId: patientIds) {

                OutputContainer output = new OutputContainer();

                //do patient delete
                output.getPatients().writeDelete(new SubscriberId((byte)0, patientId, null));
                if (testMode) {
                    LOG.debug("Would delete patient id " + patientId);
                } else {
                    //audit the ID being deleted
                    sql = "INSERT INTO " + tableForAudit + " (patient_id, table_name, id) VALUES (" + patientId + ", 'patient', " + patientId + ")";
                    statement.executeUpdate(sql);
                    connection.commit();
                }

                //do dependent tables
                for (String tableToDo: tablesToDo) {

                    Set<Long> idsToDelete = new HashSet<>();
                    sql = "SELECT id FROM " + tableToDo + " WHERE patient_id = " + patientId;
                    rs = statement.executeQuery(sql);
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        idsToDelete.add(new Long(id));
                    }
                    LOG.debug("For " + tableToDo + " found " + idsToDelete.size());

                    for (Long idToDelete: idsToDelete) {
                        long id = idToDelete.longValue();

                        if (tableToDo.equals("episode_of_care")) {
                            output.getEpisodesOfCare().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("appointment")) {
                            output.getAppointments().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("encounter")) {
                            output.getEncounters().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("allergy_intolerance")) {
                            output.getAllergyIntolerances().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("medication_statement")) {
                            output.getMedicationStatements().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("medication_order")) {
                            output.getMedicationOrders().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("flag")) {
                            output.getFlags().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("observation")) {
                            output.getObservations().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("diagnostic_order")) {
                            output.getDiagnosticOrder().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("procedure_request")) {
                            output.getProcedureRequests().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("referral_request")) {
                            output.getReferralRequests().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("patient_contact")) {
                            output.getPatientContacts().writeDelete(new SubscriberId((byte)0, id, null));
                        } else if (tableToDo.equals("patient_address")) {
                            output.getPatientAddresses().writeDelete(new SubscriberId((byte)0, id, null));
                        } else {
                            throw new Exception("Unexpected name [" + tableToDo + "]");
                        }

                        if (testMode) {
                            LOG.debug("Would delete " + tableToDo + " id " + id);

                        } else {
                            //audit the ID being deleted
                            sql = "INSERT INTO " + tableForAudit + " (patient_id, table_name, id) VALUES (" + patientId + ", '" + tableToDo + "', " + id + ")";
                            statement.executeUpdate(sql);
                            connection.commit();
                        }
                    }

                }

                //delete from the DB or queue up for sending to the remote DB
                if (!testMode) {
                    byte[] bytes = output.writeToZip();
                    if (bytes != null) {
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        UUID batchId = UUID.randomUUID();
                        for (String subscriberName : subscriberNames) {
                            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberName);
                        }
                    }

                    //mark patient as done
                    sql = "UPDATE " + tableOfPatientIds + " SET done = true WHERE patient_id = " + patientId;
                    statement.executeUpdate(sql);
                    connection.commit();
                }

                done ++;
                if (done % 1000 == 0) {
                    LOG.debug("Done " + done);
                }
            }
            LOG.debug("Finished " + done);

            statement.close();
            connection.close();

            LOG.debug("Deleting Core06 Data From Subscribers Using " + tableOfPatientIds);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    /**
     * if a service was moved from one CoreXX DB to another, without maintaining the resource ID mappings,
     * then we need to use this routine to tidy up the mess left on the subscriber DBs. SQL will need
     * to be manually run on the CoreXX DB to delete theresources, but only AFTER this has run.
     */
    /*public static void deleteDataForOldCoreDBFromSubscribers(UUID serviceId, String previousPublisherConfigName) {
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
            List<String> subscriberConfigNames = RunDataDistributionProtocols.getSubscriberConfigNamesFromOldProtocols(serviceId);
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
    }*/

    /*public static void populateMissingOrgsInCompassV1(String subscriberConfigName, boolean testMode) {
        LOG.debug("Populating Missing Orgs In CompassV1 " + subscriberConfigName + " testMode = " + testMode);
        try {

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
            if (subscriberConfig.getSubscriberType() != SubscriberConfig.SubscriberType.CompassV1) {
                throw new Exception("Expecting compassv1 but got " + subscriberConfig.getSubscriberType());
            }

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

                *//*ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getById(serviceId);*//*

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
                    EnterpriseTransformHelper helper = new EnterpriseTransformHelper(serviceId, null, null, null, subscriberConfig, resourceWrappers, false);
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

    }*/

    /*public static void populateMissingOrgsInCompassV2(String subscriberConfigName, boolean testMode) {
        LOG.debug("Populating Missing Orgs In CompassV2 " + subscriberConfigName + " testMode = " + testMode);
        try {

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
            if (subscriberConfig.getSubscriberType() != SubscriberConfig.SubscriberType.CompassV2) {
                throw new Exception("Expecting compassv2 but got " + subscriberConfig.getSubscriberType());
            }

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

                *//*ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getById(serviceId);*//*

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
                    SubscriberTransformHelper helper = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, resourceWrappers, false);

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

    }*/


    /*private static Reference findNewOrgRefOnCoreDb(String subscriberConfigName, Reference oldOrgRef, UUID serviceId, boolean testMode) throws Exception {

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
    }*/

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

    /*public static void testHashedFileFilteringForSRCode(String filePath, String uniqueKey, String dataDateStr) {
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

            long msStart = java.lang.System.currentTimeMillis();

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

            long msEnd = java.lang.System.currentTimeMillis();
            LOG.debug("Took " + ((msEnd - msStart) / 1000) + " s");

            //delete all files
            srcFile.delete();
            hashFile.delete();
            dstFile.delete();

            LOG.info("Finished Testing Hashed File Filtering for SRCode using " + filePath);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /*public static void deleteResourcesForDeletedPatients(boolean testMode, String odsCodeRegex) {
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
    }*/

    /**
     * checks to make sure that every exchange has been properly through the inbound transform without
     * being skipped, failing or being filtered on specific files
     */
    /*public static void findTppServicesNeedReprocessing(boolean showLogging, String odsCodeRegex) {
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
                        for (int j = events.size() - 1; j >= 0; j--) {
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

                        //see if we have an exchange event saying we skipped the SRCode file
                        if (transformedWithoutFiltering) {
                            Date dtTransformEnd = audit.getStarted();
                            for (int j = events.size() - 1; j >= 0; j--) {
                                ExchangeEvent event = events.get(j);
                                Date dtEvent = event.getTimestamp();
                                if (dtEvent.after(dtTransformEnd)
                                        || dtEvent.before(dtTransformStart)) {
                                    continue;
                                }

                                String eventDesc = event.getEventDesc();
                                if (eventDesc.equals("Skipped SRCode")) {
                                    logging.add("SRCode was skipped, so DIDN'T transform OK");
                                    transformedWithoutFiltering = false;
                                }
                            }
                        }
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    if (!transformedWithoutFiltering) {
                        LOG.error("FAIL " + service + " -> exchange " + exchange.getId() + " from " + sdf.format(exchange.getHeaderAsDate(HeaderKeys.DataDate)));

                    } else if (showLogging){
                        LOG.info("PASS " + service + " -> exchange " + exchange.getId() + " from " + sdf.format(exchange.getHeaderAsDate(HeaderKeys.DataDate)));
                    }

                    if (showLogging) {
                        for (String line: logging) {
                            LOG.error("    " + line);
                        }
                    }
                }
            }

            LOG.debug("Finished Finding TPP Services that Need Re-processing");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

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

    /*public static void validateProtocolCohorts() {
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
    }*/


    /*public static void compareDsmSubscribers() {
        LOG.debug("Comparing DSM for Subscribers");
        try {

            String FRAILTY_PROJECT_ID = "320baf45-37e2-4c8b-b7ee-27a9f877b95c"; //specific ID for live Frailty project

            String subscriberSystemName = "JSON_API";

            List<String> subscriberOdsCodes = new ArrayList<>();
            subscriberOdsCodes.add("ADASTRA");
            subscriberOdsCodes.add("NTP");
            subscriberOdsCodes.add("NKB");
            subscriberOdsCodes.add("8HD62");
            subscriberOdsCodes.add("YGMX6");
            subscriberOdsCodes.add("NLO");

            for (String subscriberOdsCode: subscriberOdsCodes) {

                ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service subscriberService = serviceDal.getByLocalIdentifier(subscriberOdsCode);
                LOG.debug(">>>>>>>>>>>>>>>>>>>>>> " + subscriberService);

                UUID serviceId = subscriberService.getId();

                ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(subscriberService, subscriberSystemName);
                if (endpoint == null) {
                    throw new Exception("Failed to find subscriber endpoint");
                }
                UUID requesterSystemId = endpoint.getSystemUuid();

                Set<String> serviceIdsOldWay = findPublisherServiceIdsForSubscriberOldWay(subscriberOdsCode, serviceId, requesterSystemId);
                Set<String> serviceIdsNewWay = findPublisherServiceIdsForSubscriberNewWay(subscriberOdsCode, FRAILTY_PROJECT_ID);

                List<String> oldWayList = new ArrayList<>(serviceIdsOldWay);
                List<String> newWayList = new ArrayList<>(serviceIdsNewWay);
                oldWayList.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));
                newWayList.sort(((o1, o2) -> o1.compareToIgnoreCase(o2)));

                String oldWayStr = String.join(", ", oldWayList);
                String newWayStr = String.join(", ", newWayList);

                if (oldWayStr.equals(newWayStr)) {
                    LOG.debug("Old way and new way are equal");
                    LOG.debug(oldWayStr);

                } else {
                    LOG.debug("Old way and new way are NOT equal");
                    LOG.debug("OLD way: " + oldWayStr);
                    LOG.debug("NEW way: " + newWayStr);
                }
            }

            LOG.debug("Finished Comparing DSM for Subscribers");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    private static Set<String> findPublisherServiceIdsForSubscriberNewWay(String headerOdsCode, String headerProjectId) throws Exception {

        List<String> publisherOdsCodes = ProjectCache.getAllPublishersForProjectWithSubscriberCheck(headerProjectId, headerOdsCode);

        Set<String> ret = new HashSet<>();

        for (String publisherOdsCode: publisherOdsCodes) {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            org.endeavourhealth.core.database.dal.admin.models.Service publisherService = serviceDal.getByLocalIdentifier(publisherOdsCode);
            if (publisherService == null) {
                //if the DSM is aware of a publisher that DDS isn't, then this is odd but possible
                LOG.warn("Failed to find publisher service for publisher ODS code " + publisherOdsCode);
                continue;
            }

            UUID publisherServiceId = publisherService.getId();
            ret.add(publisherServiceId.toString());
        }

        return ret;
    }

    private static Set<String> findPublisherServiceIdsForSubscriberOldWay(String headerOdsCode, UUID serviceId, UUID systemId) throws Exception {

        //find protocol
        List<Protocol> protocols = getProtocolsForSubscriberService(serviceId.toString(), systemId.toString());
        if (protocols.isEmpty()) {
            throw new Exception("No valid subscriber agreement found for requesting ODS code " + headerOdsCode);
        }

        //the below only works properly if there's a single protocol. To support multiple protocols,
        //it'll need to calculate the frailty against EACH subscriber DB and then return the one with the highest risk
        if (protocols.size() > 1) {
            throw new Exception("No support for multiple subscriber protocols in frailty calculation");
        }

        Protocol protocol = protocols.get(0);

        Set<String> ret = new HashSet<>();

        for (ServiceContract serviceContract : protocol.getServiceContract()) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                org.endeavourhealth.core.xml.QueryDocument.Service service = serviceContract.getService();
                ret.add(service.getUuid());
            }
        }

        return ret;
    }*/

    private static List<Protocol> getProtocolsForSubscriberService(String serviceUuid, String systemUuid) throws PipelineException {

        try {
            List<Protocol> ret = new ArrayList<>();

            List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, systemUuid);

            //the above fn will return is all protocols where the service and system are present, but we want to filter
            //that down to only ones where our service and system are an active publisher
            for (LibraryItem libraryItem: libraryItems) {
                Protocol protocol = libraryItem.getProtocol();
                if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

                    for (ServiceContract serviceContract : protocol.getServiceContract()) {
                        if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
                                && serviceContract.getService().getUuid().equals(serviceUuid)
                                && serviceContract.getSystem().getUuid().equals(systemUuid)
                                && serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

                            ret.add(protocol);
                            break;
                        }
                    }
                }
            }

            return ret;

        } catch (Exception ex) {
            throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
        }
    }

    /*public static void bulkSubscriberTransformAdmin(String reason, String odsCodes) {
        LOG.debug("Doing Bulk Subscriber Transform for Admin Data for " + odsCodes);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            for (Service service: services) {

                if (shouldSkipService(service, odsCodes)) {
                    continue;
                }

                LOG.debug("Doing " + service);

                UUID serviceId = service.getId();
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, false, true, true, null, new ArrayList<>(), reason);
            }

            LOG.debug("Finished Doing Bulk Subscriber Transform for Admin Data foe " + odsCodes);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    public static void populateCompassPatientPseudoIdTable(String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.debug("Populating Compass Patient Pseudo ID Table for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberPersonMappingDalI personIdDal = DalProvider.factorySubscriberPersonMappingDal(subscriberConfigName);
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "bulk load of patient_pseudo_id for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                //check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int batchSize = 0;
                org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    //check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    //retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        //throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        PatientLinkPair pair = patientLinkDal.updatePersonId(serviceId, fhirPatient);
                        discoveryPersonId = pair.getNewPersonId();
                    }
                    Long enterprisePersonId = personIdDal.findOrCreateEnterprisePersonId(discoveryPersonId);

                    //transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {

                        if (compassV1Container == null) {
                            compassV1Container = new org.endeavourhealth.transform.enterprise.outputModels.OutputContainer(subscriberConfig.isPseudonymised());
                        }

                        Long enterprisePatientId = patientIdDal.findEnterpriseIdOldWay(patientWrapper.getResourceType(), patientWrapper.getResourceId().toString());
                        if (enterprisePatientId == null) {
                            throw new Exception("Failed to find enterprisePatientId for patient " + patientWrapper.getResourceId());
                        }

                        EnterpriseTransformHelper params = new EnterpriseTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV1Container);
                        Long orgId = FhirToEnterpriseCsvTransformer.findEnterpriseOrgId(serviceId, params);
                        params.setEnterpriseOrganisationId(orgId);
                        PatientEnterpriseTransformer t = new PatientEnterpriseTransformer();
                        t.transformPseudoIds(orgId.longValue(), enterprisePatientId.longValue(), enterprisePersonId.longValue(), fhirPatient, patientWrapper, params);
                        //t.transformResources(wrappers, writer, params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV1PseudoIdData(subscriberConfigName, compassV1Container);
                            compassV1Container = null;
                            batchSize = 0;
                        }

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {

                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV2Container);
                        Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                        params.setSubscriberOrganisationId(orgId);
                        PatientTransformer t = new PatientTransformer();
                        t.transformPseudoIdsNewWay(orgId.longValue(), subscriberPatientId.getSubscriberId(), enterprisePersonId.longValue(), fhirPatient, patientWrapper, params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV2PseudoIdData(subscriberConfigName, compassV2Container);
                            compassV2Container = null;
                            batchSize = 0;
                        }

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //save any part-completed batch
                if (batchSize > 0) {

                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
                        saveCompassV1PseudoIdData(subscriberConfigName, compassV1Container);

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2PseudoIdData(subscriberConfigName, compassV2Container);

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Populating Compass Patient Pseudo ID Table for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    // For the config name provided, get the list of services which are publishers
    // and send their transformed Patient FHIR resources to the Filer
    public static void transformAndFilePatientV2IdentifiableDataForProtocolServices(boolean includeStartedButNotFinishedServices, String subscriberConfigName, String orgOdsCodeRegex) {

        LOG.debug("Populating CompassV2 patient, patient_address and patient_contact tables for identifiable data for " + subscriberConfigName + " regex " + orgOdsCodeRegex);

        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "Bulk load of patient, patient_address and patient_contact tables for identifiable data for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                if (includeStartedButNotFinishedServices) {
                    //check if already done, so we can make sure EVERY service is done
                    if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already done");
                        continue;
                    }

                } else {
                    //check if already started, to allow us to run multiple instances of this at once
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                        LOG.debug("Skipping " + service + " as already started or done");
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int done = 0;
                int batchSize = 0;
                // org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    // check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    // retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        // throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        LOG.warn("Null or empty person ID for " + fhirPatient.getId());
                        continue;
                        // throw new Exception("Null or empty person ID for " + fhirPatient.getId());
                    }

                    // transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            LOG.warn("Failed to find subscriberPatientId for " + ref);
                            continue;
                            // throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        try {
                            SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, compassV2Container);
                            Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                            params.setSubscriberOrganisationId(orgId);
                            PatientTransformer pt = new PatientTransformer();
                            pt.transformResources(wrappers, params);
                            // if batch is full then save what has been done
                            batchSize++;
                            if (batchSize >= 100) {
                                saveCompassV2PatientIdentifiableData(subscriberConfigName, compassV2Container);
                                compassV2Container = null;
                                batchSize = 0;
                            }
                        } catch (Throwable t) {
                            LOG.warn("Error with transform for patient " + patientId, t);
                        }
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }

                    done ++;
                    if (done % 1000 == 0) {
                        LOG.debug("Done " + done + " / " + patientIds.size() + " patients");
                    }
                }

                //save any part completed batch
                if (batchSize > 0) {
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2PatientIdentifiableData(subscriberConfigName, compassV2Container);
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                LOG.debug("Completed " + done + " / " + patientIds.size() + " patients");

                //audit what has been done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished populating CompassV2 patient, patient_address and patient_contact tables for identifiable data for " + subscriberConfigName + " regex " + orgOdsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void saveCompassV2PseudoIdData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.PATIENT_PSEUDO_ID);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV1PseudoIdData(String subscriberConfigName, org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container) throws Exception {
        List<String> toKeep = new ArrayList<>();
        toKeep.add("patient_pseudo_id");
        compassV1Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV1Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            EnterpriseFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }


    private static void saveCompassV2PatientData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.PATIENT);
        toKeep.add(SubscriberTableId.PATIENT_ADDRESS);
        toKeep.add(SubscriberTableId.PATIENT_CONTACT);
        toKeep.add(SubscriberTableId.REGISTRATION_STATUS_HISTORY);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV1PatientData(String subscriberConfigName, org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container) throws Exception {
        List<String> toKeep = new ArrayList<>();
        toKeep.add("patient");
        toKeep.add("patient_address");
        toKeep.add("patient_contact");
        toKeep.add("registration_status_history");
        compassV1Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV1Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            EnterpriseFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV2PatientAddressData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        // toKeep.add(SubscriberTableId.PATIENT);
        // toKeep.add(SubscriberTableId.PATIENT_ADDRESS);
        toKeep.add(SubscriberTableId.PATIENT_ADDRESS_MATCH);
        toKeep.add(SubscriberTableId.PATIENT_ADDRESS_RALF);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV2PatientAgeData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.PATIENT);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV2PatientIdentifiableData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.PATIENT);
        toKeep.add(SubscriberTableId.PATIENT_ADDRESS);
        toKeep.add(SubscriberTableId.PATIENT_CONTACT);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }


    /*public static void quickRefreshForAllTpp(String orgOdsCodeRegex) {
        LOG.info("Doing quick refresh for all TPP at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "quick refresh all subscribers for TPP missing patients";

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(service.getId(), false, false, false, null, "quick_refresh_for_any_missing_patients");

                //record we've done this
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.info("Done quick refresh for all TPP at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /*public static void quickRefreshForAllEmis(String orgOdsCodeRegex) {
        LOG.info("Doing quick refresh for all EMIS at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "quick refresh all subscribers for EMIS missing patients";

            for (Service service: services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(service.getId(), false, false, false, null, "quick_refresh_for_any_missing_patients");

                //record we've done this
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.info("Done quick refresh for all EMIS at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /*public static void testInformationModelFromJson(String filePath) {
        LOG.info("Testing IM from JSON in " + filePath);
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                throw new Exception("" + filePath + " does not exist");
            }
            String json = FileHelper.readTextFile(f.toPath());
            Resource r = FhirSerializationHelper.deserializeResource(json);

            CodeableConcept cc = null;
            if (r instanceof Procedure) {
                cc = ((Procedure)r).getCode();
            } else if (r instanceof Observation) {
                cc = ((Observation)r).getCode();
            } else {
                throw new Exception("Unexpected resource type " + r.getResourceType());
            }

            Coding originalCoding = ObservationCodeHelper.findOriginalCoding(cc);
            if (originalCoding == null) {
                throw new Exception("Failed to find original coding");
            }
            String originalCode = originalCoding.getCode();
            String conceptScheme = ObservationCodeHelper.mapCodingSystemToImScheme(originalCoding);
            String display = originalCoding.getDisplay();
            LOG.debug("IM concept scheme = " + conceptScheme);

            Integer coreConceptId = IMHelper.getIMMappedConcept(null, r, conceptScheme, originalCode);
            LOG.debug("Found core concept ID " + coreConceptId);

            Integer nonCoreConceptId = IMHelper.getIMConcept(null, r, conceptScheme, originalCode, display);
            LOG.debug("Found non-core concept ID " + nonCoreConceptId);

            LOG.info("Finished Testing IM from JSON in " + filePath);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /**
     * populates new fields added to eds.patient_search table
     */
    /*public static void populatePatientSearchFields() {
        LOG.info("Populating Patient Search Fields");
        try {

            Connection edsConnection = ConnectionManager.getEdsNonPooledConnection();

            int done = 0;
            int errors = 0;
            while (true) {

                int batchSize = 100;
                String sql = "SELECT service_id, patient_id FROM patient_search WHERE dt_created IS NULL LIMIT " + batchSize;
                PreparedStatement ps = edsConnection.prepareStatement(sql);

                Map<UUID, UUID> hmPatientsAndServices = new HashMap<>();

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String serviceId = rs.getString(1);
                    String patientId = rs.getString(2);
                    hmPatientsAndServices.put(UUID.fromString(patientId), UUID.fromString(serviceId));
                }
                ps.close();

                sql = "UPDATE patient_search"
                        + " SET nhs_number_verification_status = ?, dt_created = ?, test_patient = ?"
                        + " WHERE patient_id = ?"
                        + " AND service_id = ?";
                ps = edsConnection.prepareStatement(sql);

                for (UUID patientId: hmPatientsAndServices.keySet()) {
                    UUID serviceId = hmPatientsAndServices.get(patientId);

                    boolean testPatient = false;
                    Date dtCreated = null;
                    NhsNumberVerificationStatus verificationStatus = null;

                    //get latest version of patient
                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, ResourceType.Patient.toString(), patientId);
                    if (history.isEmpty()) {
                        LOG.error("Empty history list for patient " + patientId + " at service " + serviceId);
                        dtCreated = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01");
                        errors ++;

                    } else {

                        //find most recent non-deleted version
                        ResourceWrapper lastWrapper = null;
                        for (ResourceWrapper w : history) {
                            if (!w.isDeleted()) {
                                lastWrapper = w;
                                break;
                            }
                        }

                        if (lastWrapper == null) {
                            LOG.error("Failed to find non-deleted patient " + patientId + " at service " + serviceId);
                            dtCreated = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-02");
                            errors++;

                        } else {

                            //update specific fields on patient search
                            Patient resource = (Patient) lastWrapper.getResource();

                            verificationStatus = IdentifierHelper.findNhsNumberVerificationStatus(resource);

                            BooleanType testPatientVal = (BooleanType) ExtensionConverter.findExtensionValue(resource, FhirExtensionUri.PATIENT_IS_TEST_PATIENT);
                            testPatient = testPatientVal != null
                                    && testPatientVal.hasValue()
                                    && testPatientVal.getValue().booleanValue();

                            ResourceWrapper firstWrapper = history.get(history.size() - 1);
                            dtCreated = firstWrapper.getCreatedAt();
                        }
                    }

                    int col = 1;
                    if (verificationStatus == null) {
                        ps.setNull(col++, Types.VARCHAR);
                    } else {
                        ps.setString(col++, verificationStatus.getCode());
                    }
                    ps.setTimestamp(col++, new java.sql.Timestamp(dtCreated.getTime()));
                    ps.setBoolean(col++, testPatient);
                    ps.setString(col++, patientId.toString());
                    ps.setString(col++, serviceId.toString());

                    ps.addBatch();

                    done ++;
                    if (done % 10000 == 0) {
                        LOG.debug("Done " + done + " with " + errors + " errors");
                    }
                }

                ps.executeBatch();
                edsConnection.commit();

                if (hmPatientsAndServices.size() < batchSize) {
                    break;
                }
            }

            edsConnection.close();

            LOG.debug("Done " + done + " with " + errors + " errors");
            LOG.info("Finished Populating Patient Search Fields");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /*public static void testUprnToken(String protocol) {
        LOG.info("Testing UPRN Token");
        try {

            String adrec = "60 Locksons Close, London, E146BH";
            String ids = "2196436781`60944`ceg_enterprise";

            //test old way
            LOG.debug("Doing UORN >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            for (int i=0; i<80; i++) {
                try {
                    String csv = UPRN.getAdrec(adrec, ids);
                    LOG.debug("Got response " + csv);

                } catch (Exception ex) {
                    LOG.error("", ex);
                    break;
                }
                LOG.debug("");
                Thread.sleep(10 * 1000);
            }

            LOG.info("Finished Testing UPRN Token");
        } catch(Throwable t) {
            LOG.error("", t);
        }
    }*/



    /**
     * the Emis CSV time format was wrong (https://endeavourhealth.atlassian.net/browse/SD-108)
     * which meant any appt or schedule with a time of "12xxxx" was saved as "00xxxx"
     */
    /*public static void fixEmisAppointmentsAt12(String orgOdsCodeRegex) {
        LOG.info("Fixing Emis Appointment Times for " + orgOdsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

            String bulkOperationName = "fix Emis appointment times";

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                //check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (systemIds.size() != 1) {
                    throw new Exception("" + systemIds.size() + " system IDs found");
                }
                UUID systemId = systemIds.get(0);

                List<UUID> batchIdsCreated = new ArrayList<>();

                FhirResourceFiler filer = null;
                Exchange newExchange = null;

                UUID exchangeId = UUID.randomUUID();
                String bodyJson = JsonSerializer.serialize(new ArrayList<ExchangePayloadFile>());
                String odsCode = service.getLocalId();

                filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                newExchange = new Exchange();
                newExchange.setId(exchangeId);
                newExchange.setBody(bodyJson);
                newExchange.setTimestamp(new Date());
                newExchange.setHeaders(new HashMap<>());
                newExchange.setHeaderAsUuid(HeaderKeys.SenderServiceUuid, service.getId());
                newExchange.setHeader(HeaderKeys.ProtocolIds, ""); //just set to non-null value, so postToExchange(..) can safely recalculate
                newExchange.setHeader(HeaderKeys.SenderLocalIdentifier, odsCode);
                newExchange.setHeaderAsUuid(HeaderKeys.SenderSystemUuid, systemId);
                newExchange.setHeader(HeaderKeys.SourceSystem, MessageFormat.EMIS_CSV);
                newExchange.setServiceId(service.getId());
                newExchange.setSystemId(systemId);

                AuditWriter.writeExchange(newExchange);
                AuditWriter.writeExchangeEvent(newExchange, "Manually created to correct Emis appointment start times (SD-108)");

                Set<String> appointmentsIdsDone = new HashSet<>();
                Set<String> scheduleIdDone = new HashSet<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                int done = 0;

                try {

                    List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
                    LOG.debug("Found " + exchanges.size() + " exchanges");
                    for (Exchange x : exchanges) {

                        String exchangeBody = x.getBody();
                        List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

                        ExchangePayloadFile slotFile = null;
                        ExchangePayloadFile scheduleFile = null;
                        for (ExchangePayloadFile file : files) {
                            if (file.getType().equals("Appointment_Slot")) {
                                slotFile = file;
                            } else if (file.getType().equals("Appointment_Session")) {
                                scheduleFile = file;
                            }
                        }

                        if (slotFile != null) {

                            String path = slotFile.getPath();
                            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);

                            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                            Iterator<CSVRecord> iterator = parser.iterator();

                            while (iterator.hasNext()) {
                                CSVRecord record = iterator.next();

                                String slotGuid = record.get("SlotGuid");
                                String appointmentDateStr = record.get("AppointmentDate");
                                String appointmentStartTimeStr = record.get("AppointmentStartTime");
                                String plannedDurationInMinutesStr = record.get("PlannedDurationInMinutes");
                                String patientGuid = record.get("PatientGuid");
                                String sendInTimeStr = record.get("SendInTime");
                                String leftTimeStr = record.get("LeftTime");
                                //String didNotAttend = record.get("DidNotAttend");
                                //String patientWaitInMin = record.get("PatientWaitInMin");
                                //String appointmentDelayInMin = record.get("AppointmentDelayInMin");
                                //String actualDurationInMinutes = record.get("ActualDurationInMinutes");
                                //String organisationGuid = record.get("OrganisationGuid");
                                //String sessionGuid = record.get("SessionGuid");
                                //String dnaReasonCodeId = record.get("DnaReasonCodeId");
                                String deleted = record.get("Deleted");
                                //String processingId = record.get("ProcessingId");

                                if (Strings.isNullOrEmpty(patientGuid)) {
                                    continue;
                                }

                                String sourceId = patientGuid + ":" + slotGuid;
                                if (appointmentsIdsDone.contains(sourceId)) {
                                    continue;
                                }
                                appointmentsIdsDone.add(sourceId);

                                if (deleted.equals("true")) {
                                    continue;
                                }

                                //if nothing will have been affected by the bug, skip it
                                if (!appointmentStartTimeStr.startsWith("12")
                                        && !sendInTimeStr.startsWith("12")
                                        && !leftTimeStr.startsWith("12")) {
                                    continue;
                                }

                                UUID appointmentUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Appointment, sourceId);
                                if (appointmentUuid == null) {
                                    //we received data for appts where we didn't have the patient, so skipped them
                                    //throw new Exception("Failed to find UUID for appointment " + sourceId);
                                    continue;
                                }
                                Appointment appointment = (Appointment) resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Appointment, appointmentUuid.toString());
                                if (appointment == null) {
                                    //appt may be null if the patient record has been deleted, in which case just skip it
                                    continue;
                                    //throw new Exception("Failed to find appointment " + appointmentUuid);
                                }

                                UUID slotUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Slot, sourceId);
                                if (slotUuid == null) {
                                    //if we've passed the above checks and got a non-deleted Appointment, then something is wrong if this is null
                                    throw new Exception("Failed to find UUID for slot " + sourceId);
                                }
                                Slot slot = (Slot) resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Slot, slotUuid.toString());
                                if (slot == null) {
                                    //if we've passed the above checks and got a non-deleted Appointment, then something is wrong if this is null
                                    throw new Exception("Failed to find slot " + slotUuid);
                                }

                                Date d = dateFormat.parse(appointmentDateStr);
                                Date startTime = timeFormat.parse(appointmentStartTimeStr);
                                Date startDateTime = new Date(d.getTime() + startTime.getTime());

                                AppointmentBuilder appointmentBuilder = new AppointmentBuilder(appointment);
                                SlotBuilder slotBuilder = new SlotBuilder(slot);

                                appointmentBuilder.setStartDateTime(startDateTime);
                                slotBuilder.setStartDateTime(startDateTime);

                                int plannedDurationMins = Integer.parseInt(plannedDurationInMinutesStr);
                                long endMillis = startDateTime.getTime() + (plannedDurationMins * 60 * 1000);

                                slotBuilder.setEndDateTime(new Date(endMillis));
                                appointmentBuilder.setEndDateTime(new Date(endMillis));

                                if (!Strings.isNullOrEmpty(sendInTimeStr)) {
                                    Date sendInTime = timeFormat.parse(sendInTimeStr);
                                    Date sendInDateTime = new Date(d.getTime() + sendInTime.getTime());
                                    appointmentBuilder.setSentInDateTime(sendInDateTime);
                                }

                                if (!Strings.isNullOrEmpty(leftTimeStr)) {
                                    Date leftTime = timeFormat.parse(leftTimeStr);
                                    Date leftDateTime = new Date(d.getTime() + leftTime.getTime());
                                    appointmentBuilder.setLeftDateTime(leftDateTime);
                                }

                                filer.savePatientResource(null, false, appointmentBuilder, slotBuilder);
                            }

                            parser.close();
                        }


                        if (scheduleFile != null) {
                            String path = scheduleFile.getPath();
                            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path);

                            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
                            Iterator<CSVRecord> iterator = parser.iterator();

                            while (iterator.hasNext()) {
                                CSVRecord record = iterator.next();

                                String appointmentSessionGuid = record.get("AppointmentSessionGuid");
                                //String description = record.get("Description");
                                //String locationGuid = record.get("LocationGuid");
                                //String sessionTypeDescription = record.get("SessionTypeDescription");
                                //String sessionCategoryDisplayName = record.get("SessionCategoryDisplayName");
                                String startDateStr = record.get("StartDate");
                                String startTimeStr = record.get("StartTime");
                                String endDateStr = record.get("EndDate");
                                String endTimeStr = record.get("EndTime");
                                //String private = record.get("Private");
                                //String organisationGuid = record.get("OrganisationGuid");
                                String deleted = record.get("Deleted");
                                //String processingId = record.get("ProcessingId");

                                String sourceId = appointmentSessionGuid;
                                if (scheduleIdDone.contains(sourceId)) {
                                    continue;
                                }
                                scheduleIdDone.add(sourceId);

                                if (deleted.equals("true")) {
                                    continue;
                                }

                                //if nothing will have been affected by the bug, skip it
                                if (!startTimeStr.startsWith("12")
                                        && !endTimeStr.startsWith("12")) {
                                    continue;
                                }

                                UUID scheduleUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Schedule, sourceId);

                                if (scheduleUuid == null) {
                                    throw new Exception("Failed to find UUID for schedule " + sourceId);
                                }

                                Schedule schedule = (Schedule) resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Schedule, scheduleUuid.toString());

                                if (schedule == null) {
                                    throw new Exception("Failed to find schedule " + scheduleUuid);
                                }

                                ScheduleBuilder scheduleBuilder = new ScheduleBuilder(schedule);

                                Date startDate = dateFormat.parse(startDateStr);
                                Date startTime = timeFormat.parse(startTimeStr);
                                Date startDateTime = new Date(startDate.getTime() + startTime.getTime());
                                scheduleBuilder.setPlanningHorizonStart(startDateTime);

                                Date endDate = dateFormat.parse(endDateStr);
                                Date endTime = timeFormat.parse(endTimeStr);
                                Date endDateTime = new Date(endDate.getTime() + endTime.getTime());
                                scheduleBuilder.setPlanningHorizonEnd(endDateTime);

                                filer.saveAdminResource(null, false, scheduleBuilder);
                            }

                            parser.close();
                        }

                        done++;
                        if (done % 100 == 0) {
                            LOG.debug("Done " + done + " exchanges");
                        }
                    }
                    LOG.debug("Finished " + done + " exchanges");

                } finally {

                    //close down filer
                    filer.waitToFinish();

                    //set multicast header
                    String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                    newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                    //post to Rabbit protocol queue
                    List<UUID> exchangeIds = new ArrayList<>();
                    exchangeIds.add(newExchange.getId());
                    QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);

                    //set this after posting to rabbit so we can't re-queue it later
                    newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                    newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                    AuditWriter.writeExchange(newExchange);
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.info("Finished Fixing Emis Appointment Times for " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /**
     * when the Compass DBs were changed to NOT exclude data items flagged as confidential, it looks like
     * some data items didn't get refreshed and sent to the DB. This routine simply goes through each patient record
     * to find out if there is a confidential data item and stores the patient UUID in a table. These can then
     * be refreshed to all subscribers using the existing routine
     * https://endeavourhealth.atlassian.net/browse/SD-111
     */
    /*public static void findPatientsWithConfidentialData(String orgOdsCodeRegex) {
        LOG.info("Finding Patients With Confidential Data at " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            //ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            List<Service> services = serviceDal.getAll();

            String bulkOperationName = "finding patients with confidential data (SD-111)";

            for (Service service: services) {

                //only Barts DW and Emis have confidential items, so skip if not them
                Map<String, String> tags = service.getTags();
                if (tags == null
                        || (!tags.containsKey("EMIS")
                        && !tags.containsKey("Barts DW"))) {
                    continue;
                }

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                UUID serviceId = service.getId();
                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int done = 0;
                int countFound = 0;
                for (UUID patientId: patientIds) {

                    //way too slow to retrieve all resources and check in code
                    //List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId);
                    String sql = "SELECT 1 FROM resource_current WHERE service_id = ? AND patient_id = ? AND resource_data LIKE ?";

                    Connection conn = ConnectionManager.getEhrConnection(serviceId);
                    PreparedStatement ps = conn.prepareStatement(sql);

                    ps.setString(1, serviceId.toString());
                    ps.setString(2, patientId.toString());
                    ps.setString(3, "%" + FhirExtensionUri.IS_CONFIDENTIAL + "%");

                    ResultSet rs = ps.executeQuery();
                    boolean found = rs.next();

                    ps.close();
                    conn.close();

                    if (found) {
                        countFound ++;
                        conn = ConnectionManager.getAuditConnection();

                        sql = "INSERT INTO confidential_patients (service_id, patient_id) VALUES (?, ?)";
                        ps = conn.prepareStatement(sql);

                        ps.setString(1, serviceId.toString());
                        ps.setString(2, patientId.toString());

                        ps.executeUpdate();
                        conn.commit();

                        ps.close();
                        conn.close();
                    }

                    done ++;
                    if (done % 1000 == 0) {
                        LOG.debug("Done " + done + " found " + countFound);
                    }
                }
                LOG.debug("Finished " + done + " found " + countFound);

                //record we've done this
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.info("Finished Finding Patients With Confidential Data at " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }*/

    /**
     * populates the new audit tables for DPA and DSAs
     */
    /*public static void populateSubscriberAuditTables() {
        LOG.info("Populating Subscriber Audit Tables");
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            //ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service: services) {

                if (Strings.isNullOrEmpty(service.getPublisherConfigName())) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                LOG.debug("Doing " + service);


                boolean hasDpa = PublisherHelper.hasDpa(null, service.getId(), service.getLocalId());
                ServicePublisherAuditDalI dal = DalProvider.factoryServicePublisherAuditDal();
                dal.saveDpaState(service.getId(), true);

                List<String> configNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId(), service.getLocalId());
                ServiceSubscriberAuditDalI dal2 = DalProvider.factoryServiceSubscriberAuditDal();
                dal2.saveSubscribers(service.getId(), configNames);
            }

            LOG.info("Finished Populating Subscriber Audit Tables");
        } catch (Throwable t) {
            LOG.error("", t);
        }

    }*/

    /**
     * the SQL used to determine the Frailty flag, used by the Frailty API has been refactored, so this tests
     * all patients that have had results to ensure the same result is found now
     */
    /*public static void testNewFrailtySql() {
        LOG.info("Testing New Frailty SQL");
        try {

            //get results from Frailty audit table
            Map<String, Boolean> hmResults = new HashMap<>();

            String sql = "select request_path, response_body"
                    + " from audit.subscriber_api_audit"
                    + " where timestmp > '2020-08-01'";
            Connection connection = ConnectionManager.getAuditConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                String request = rs.getString(1);
                String response = rs.getString(2);

                int index = request.indexOf("subject=");
                String nhsNumber = request.substring(index + 8);

                Boolean frail = null;
                if (response.contains("No patient record could be found for NHS number")) {
                    frail = null;

                } else {
                    frail = new Boolean(response.contains("Potentially frail"));
                }
                hmResults.put(nhsNumber, frail);
            }
            ps.close();
            connection.close();
            LOG.debug("Found " + hmResults.size() + " messages to test");

            String queryConfigName = "frailtyQueryCompassv1";
            String requestingOdsCode = "RRU";

            Set<UUID> publisherServiceIds = SubscriberHelper.findPublisherServiceIdsForSubscriber(requestingOdsCode, "320baf45-37e2-4c8b-b7ee-27a9f877b95c");
            LOG.debug("Found " + publisherServiceIds.size() + " publishers");
            LOG.debug("" + String.join(", " + publisherServiceIds));

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

            //loop, testing each one
            for (String nhsNumber: hmResults.keySet()) {

                Boolean wasFrail = hmResults.get(nhsNumber);
                Boolean nowFrail = null;
                List<String> logging = new ArrayList<>();

                //find patient IDs
                Map<UUID, UUID> patientAndServiceUuids = patientSearchDal.findPatientIdsForNhsNumber(publisherServiceIds, nhsNumber);
                logging.add("Found " + patientAndServiceUuids.size() + " patient UUIDs");

                if (patientAndServiceUuids.isEmpty()) {
                    nowFrail = null;
                    logging.add("No search result found");

                } else {

                    Set<String> results = new HashSet<>();

                    Set<String> endpoints = getEnterpriseEndpoints(patientAndServiceUuids);
                    for (String endpoint : endpoints) {

                        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(endpoint);
                        SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfig.getSubscriberConfigName());

                        for (UUID patientUuid : patientAndServiceUuids.keySet()) {

                            Long enterpriseId = patientIdDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), patientUuid.toString());
                            if (enterpriseId == null) {
                                logging.add("" + patientUuid + " has no enterprise ID");
                                continue;
                            }
                            logging.add("" + patientUuid + " has enterprise ID -> " + enterpriseId);

                            sql = ConfigManager.getConfiguration(queryConfigName, "messaging-api");
                            if (Strings.isNullOrEmpty(sql)) {
                                throw new Exception("Failed to find query for config name [" + queryConfigName + "]");
                            }

                            connection = ConnectionManager.getSubscriberConnection(subscriberConfig.getSubscriberConfigName());
                            ps = null;
                            try {
                                ps = connection.prepareStatement(sql);
                                ps.setLong(1, enterpriseId.longValue());

                                rs = ps.executeQuery();
                                if (rs.next()) {
                                    String result = rs.getString(1);
                                    results.add(result);
                                    logging.add("For enterprise ID " + enterpriseId + " got SQL result " + result);
                                }

                            } finally {
                                if (ps != null) {
                                    ps.close();
                                }
                                connection.close();
                            }
                        }
                    }

                    if (results.contains("1_MILD")
                            || results.contains("2_MODERATE")
                            || results.contains("3_SEVERE")) {
                        nowFrail = Boolean.TRUE;
                        logging.add("Results contains a frailty result, so TRUE");

                    } else {
                        nowFrail = Boolean.FALSE;
                        logging.add("Results don't contain a frailty result, so FALSE");
                    }
                }

                if (wasFrail == null) {

                    if (nowFrail == null) {
                        LOG.info("" + nhsNumber + " no search result found OK");
                    } else {
                        LOG.error("" + nhsNumber + " no search result before but now frail = " + nowFrail);
                        for (String line: logging) {
                            LOG.error("    " + line);
                        }
                    }

                } else {

                    if (nowFrail != null
                            && nowFrail.equals(wasFrail)) {
                        LOG.info("" + nhsNumber + " was frail = " + wasFrail + " OK");

                    } else {
                        LOG.error("" + nhsNumber + " was frail = " + wasFrail + " now frail " + nowFrail);
                        for (String line: logging) {
                            LOG.error("    " + line);
                        }
                    }
                }
            }

            LOG.info("Finished Testing New Frailty SQL");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static Set<String> getEnterpriseEndpoints(Map<UUID, UUID> patientSearchResults) throws Exception {

        Set<String> ret = new HashSet<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();

        //we have a config record telling us what subscribers we're allowed to use
        Set<String> permittedSubscribers = getPermittedSubscribers();

        //find all subscriber configs for each of the services that has a record for the person
        List<UUID> patientServiceIds = new ArrayList<>(patientSearchResults.values());
        for (UUID patientServiceId : patientServiceIds) {

            org.endeavourhealth.core.database.dal.admin.models.Service service = serviceDal.getById(patientServiceId);
            String odsCode = service.getLocalId();
            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, patientServiceId, odsCode);

            for (String subscriberConfigName : subscriberConfigNames) {
                if (!permittedSubscribers.contains(subscriberConfigName)) {
                    continue;
                }

                ret.add(subscriberConfigName);
            }
        }

        return ret;
    }

    private static Set<String> getPermittedSubscribers() throws Exception {

        Set<String> ret = new HashSet<>();

        JsonNode json = ConfigManager.getConfigurationAsJson("frailtyCompassDatabases", "messaging-api");
        for (int i=0; i<json.size(); i++) {
            String subscriber = json.get(i).asText();
            ret.add(subscriber);
        }

        return ret;
    }*/





    /**
     * re-sends Appointment data to subscriber DBs to correct for a bug that meant the time component was dropped (https://endeavourhealth.atlassian.net/browse/SD-106)
     */
    /*public static void fixAppointmentTimesInCompass(String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.info("Fixing Compass Appointment Times for " + orgOdsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberPersonMappingDalI personIdDal = DalProvider.factorySubscriberPersonMappingDal(subscriberConfigName);
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            Date dtFixed = new SimpleDateFormat("yyyy-MM-dd").parse("2020-04-01");

            String bulkOperationName = "fix missing appointment times in " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId(), service.getLocalId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

                //check if already done
                if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping " + service + " as already done");
                    continue;
                }

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int batchSize = 0;
                org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    //check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    //retrieve FHIR appointments
                    List<ResourceWrapper> appointmentWrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Appointment.toString());

                    //remove any wrappers updated since the bug was fixed
                    for (int i=appointmentWrappers.size()-1; i>=0; i--) {
                        ResourceWrapper w = appointmentWrappers.get(i);
                        if (w.getCreatedAt().after(dtFixed)) {
                            appointmentWrappers.remove(i);
                        }
                    }

                    if (appointmentWrappers.isEmpty()) {
                        continue;
                    }

                    //transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {

                        if (compassV1Container == null) {
                            compassV1Container = new org.endeavourhealth.transform.enterprise.outputModels.OutputContainer(subscriberConfig.isPseudonymised());
                        }

                        EnterpriseTransformHelper params = new EnterpriseTransformHelper(serviceId, null, null, null, subscriberConfig, appointmentWrappers, false, compassV1Container);
                        Long orgId = FhirToEnterpriseCsvTransformer.findEnterpriseOrgId(serviceId, params);
                        params.setEnterpriseOrganisationId(orgId);
                        params.populatePatientAndPersonIds();

                        AbstractEnterpriseCsvWriter writer = compassV1Container.getAppointments();

                        AppointmentEnterpriseTransformer t = new AppointmentEnterpriseTransformer();
                        t.transformResources(new ArrayList(appointmentWrappers), writer, params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV1AppointmentData(subscriberConfigName, compassV1Container);
                            compassV1Container = null;
                            batchSize = 0;
                        }

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {

                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = ReferenceHelper.createResourceReference(ResourceType.Patient, patientId.toString());
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, appointmentWrappers, false, compassV2Container);
                        params.populatePatientAndPersonIds();

                        Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                        params.setSubscriberOrganisationId(orgId);
                        AppointmentTransformer t = new AppointmentTransformer();
                        t.transformResources(new ArrayList(appointmentWrappers), params);

                        //if batch is full then save what we've done
                        batchSize++;
                        if (batchSize >= 100) {
                            saveCompassV2AppointmentData(subscriberConfigName, compassV2Container);
                            compassV2Container = null;
                            batchSize = 0;
                        }

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //save any part-completed batch
                if (batchSize > 0) {

                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
                        saveCompassV1AppointmentData(subscriberConfigName, compassV1Container);

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2AppointmentData(subscriberConfigName, compassV2Container);

                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                //audit that we've done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.info("Finished Fixing Compass Appointment Times for " + orgOdsCodeRegex);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    private static void saveCompassV2AppointmentData(String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.APPOINTMENT);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

    private static void saveCompassV1AppointmentData(String subscriberConfigName, org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container) throws Exception {
        List<String> toKeep = new ArrayList<>();
        toKeep.add("appointment");
        compassV1Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV1Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            EnterpriseFiler.file(batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }*/
}
