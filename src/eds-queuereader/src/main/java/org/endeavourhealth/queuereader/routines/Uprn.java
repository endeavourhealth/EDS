package org.endeavourhealth.queuereader.routines;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.reference.PostcodeDalI;
import org.endeavourhealth.core.database.dal.reference.models.PostcodeLookup;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberOrgMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberPersonMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.transform.common.TransformConfig;
import org.endeavourhealth.transform.subscriber.BulkHelper;
import org.hibernate.internal.SessionImpl;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;

public class Uprn extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(Uprn.class);

    public static void calculateUprnPseudoIds(String subscriberConfigName, String targetTable) throws Exception {
        LOG.info("Calculating UPRN Pseudo IDs " + subscriberConfigName);
        try {

            JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
            JsonNode pseudoNode = config.get("pseudonymisation");
            if (pseudoNode == null) {
                LOG.error("No salt key found!");
                return;
            }
            JsonNode saltNode = pseudoNode.get("salt");
            String base64Salt = saltNode.asText();
            byte[] saltBytes = Base64.getDecoder().decode(base64Salt);

            EntityManager subscrberEntityManager = ConnectionManager.getSubscriberTransformEntityManager(subscriberConfigName);
            SessionImpl session = (SessionImpl) subscrberEntityManager.getDelegate();
            Connection subscriberConnection = session.connection();

            String upsertSql = "INSERT INTO " + targetTable + " (uprn, pseudo_uprn, property_class) VALUES (?, ?, ?)";

            PreparedStatement psUpsert = subscriberConnection.prepareStatement(upsertSql);
            int inBatch = 0;
            int done = 0;

            EntityManager referenceEntityManager = ConnectionManager.getReferenceEntityManager();
            session = (SessionImpl) referenceEntityManager.getDelegate();
            Connection referenceConnection = session.connection();

            String selectSql = "SELECT uprn, property_class FROM uprn_property_class";

            PreparedStatement psSelect = referenceConnection.prepareStatement(selectSql);
            psSelect.setFetchSize(2000);

            LOG.info("Starting query on EDS database");
            ResultSet rs = psSelect.executeQuery();
            LOG.info("Got raw results back");

            while (rs.next()) {
                long uprn = rs.getLong(1);
                String cls = rs.getString(2);

                String pseuoUprn = null;
                TreeMap<String, String> keys = new TreeMap<>();
                keys.put("UPRN", "" + uprn);

                Crypto crypto = new Crypto();
                crypto.SetEncryptedSalt(saltBytes);
                pseuoUprn = crypto.GetDigest(keys);

                psUpsert.setLong(1, uprn);
                psUpsert.setString(2, pseuoUprn);
                psUpsert.setString(3, cls);

                psUpsert.addBatch();
                inBatch++;
                done++;

                if (inBatch >= TransformConfig.instance().getResourceSaveBatchSize()) {
                    psUpsert.executeBatch();
                    subscriberConnection.commit();
                    inBatch = 0;
                }

                if (done % 5000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            if (inBatch > 0) {
                psUpsert.executeBatch();
                subscriberConnection.commit();
            }
            LOG.debug("Done " + done);

            psUpsert.close();
            subscrberEntityManager.close();

            psSelect.close();
            referenceEntityManager.close();

            LOG.info("Finished Calculating UPRN Pseudo IDs " + subscriberConfigName);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void populateSubscriberUprnTable(String subscriberConfigName, Integer overrideBatchSize, String specificPatientId) throws Exception {
        LOG.info("Populating Subscriber UPRN Table for " + subscriberConfigName);
        try {

            int saveBatchSize = TransformConfig.instance().getResourceSaveBatchSize();
            if (overrideBatchSize != null) {
                saveBatchSize = overrideBatchSize.intValue();
            }

            JsonNode config = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");

            //changed the format of the JSON
            JsonNode pseudoNode = config.get("pseudonymisation");
            boolean pseudonymised = pseudoNode != null;
            byte[] saltBytes = null;

            if (pseudonymised) {
                JsonNode saltNode = pseudoNode.get("salt");
                String base64Salt = saltNode.asText();
                saltBytes = Base64.getDecoder().decode(base64Salt);
            }
			/*boolean pseudonymised = config.get("pseudonymised").asBoolean();

			byte[] saltBytes = null;
			if (pseudonymised) {
				JsonNode saltNode = config.get("salt");
				String base64Salt = saltNode.asText();
				saltBytes = Base64.getDecoder().decode(base64Salt);
			}*/

            List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections(subscriberConfigName);
            for (EnterpriseConnector.ConnectionWrapper connectionWrapper : connectionWrappers) {
                Connection subscriberConnection = connectionWrapper.getConnection();

                //we don't have a way to update the age for subscribers that don't have direct DB connectivity
                if (!connectionWrapper.hasDatabaseConnection()) {
                    throw new Exception("Cannot update subscriber " + connectionWrapper + " for config " + subscriberConfigName + " without direct database connectivity");
                }

                LOG.info("Populating " + connectionWrapper);

                String upsertSql;
                if (pseudonymised) {
                    upsertSql = "INSERT INTO patient_uprn"
                            + " (patient_id, organization_id, person_id, lsoa_code, pseudo_uprn, qualifier, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode, property_class)"
                            + " VALUES"
                            + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                            + " ON DUPLICATE KEY UPDATE"
                            + " organization_id = VALUES(organization_id),"
                            + " person_id = VALUES(person_id),"
                            + " lsoa_code = VALUES(lsoa_code),"
                            + " pseudo_uprn = VALUES(pseudo_uprn),"
                            + " qualifier = VALUES(qualifier),"
                            + " `algorithm` = VALUES(`algorithm`),"
                            + " `match` = VALUES(`match`),"
                            + " no_address = VALUES(no_address),"
                            + " invalid_address = VALUES(invalid_address),"
                            + " missing_postcode = VALUES(missing_postcode),"
                            + " invalid_postcode = VALUES(invalid_postcode),"
                            + " property_class = VALUES(property_class)";

                } else {
                    upsertSql = "INSERT INTO patient_uprn"
                            + " (patient_id, organization_id, person_id, lsoa_code, uprn, qualifier, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode, property_class)"
                            + " VALUES"
                            + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                            + " ON DUPLICATE KEY UPDATE"
                            + " organization_id = VALUES(organization_id),"
                            + " person_id = VALUES(person_id),"
                            + " lsoa_code = VALUES(lsoa_code),"
                            + " uprn = VALUES(uprn),"
                            + " qualifier = VALUES(qualifier),"
                            + " `algorithm` = VALUES(`algorithm`),"
                            + " `match` = VALUES(`match`),"
                            + " no_address = VALUES(no_address),"
                            + " invalid_address = VALUES(invalid_address),"
                            + " missing_postcode = VALUES(missing_postcode),"
                            + " invalid_postcode = VALUES(invalid_postcode),"
                            + " property_class = VALUES(property_class)";
                }

                PreparedStatement psUpsert = subscriberConnection.prepareStatement(upsertSql);
                int inBatch = 0;

                EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
                SessionImpl session = (SessionImpl) edsEntityManager.getDelegate();
                Connection edsConnection = session.connection();

                SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
                PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
                PostcodeDalI postcodeDal = DalProvider.factoryPostcodeDal();

                int checked = 0;
                int saved = 0;

                Map<String, Boolean> hmPermittedPublishers = new HashMap<>();

                //join to the property class table - this isn't the best way of doing it as it will only work while
                //the reference and eds databases are on the same server
                //String sql = "SELECT service_id, patient_id, uprn, qualifier, abp_address, `algorithm`, `match`, no_address, invalid_address, missing_postcode, invalid_postcode FROM patient_address_uprn";
                String sql = "SELECT a.service_id, a.patient_id, a.uprn, a.qualifier, a.abp_address, a.`algorithm`,"
                        + " a.`match`, a.no_address, a.invalid_address, a.missing_postcode, a.invalid_postcode, c.property_class"
                        + " FROM patient_address_uprn a"
                        + " LEFT OUTER JOIN reference.uprn_property_class c"
                        + " ON c.uprn = a.uprn";

                //support one patient at a time for debugging
                if (specificPatientId != null) {
                    sql += " WHERE a.patient_id = '" + specificPatientId + "'";
                    LOG.debug("Restricting to patient " + specificPatientId);
                }

                Statement s = edsConnection.createStatement();
                s.setFetchSize(2000); //don't get all rows at once

                LOG.info("Starting query on EDS database");
                ResultSet rs = s.executeQuery(sql);
                LOG.info("Got raw results back");

                while (rs.next()) {
                    int col = 1;
                    String serviceId = rs.getString(col++);
                    String patientId = rs.getString(col++);
                    Long uprn = rs.getLong(col++);
                    if (rs.wasNull()) {
                        uprn = null;
                    }
                    String qualifier = rs.getString(col++);
                    String abpAddress = rs.getString(col++);
                    String algorithm = rs.getString(col++);
                    String match = rs.getString(col++);
                    boolean noAddress = rs.getBoolean(col++);
                    boolean invalidAddress = rs.getBoolean(col++);
                    boolean missingPostcode = rs.getBoolean(col++);
                    boolean invalidPostcode = rs.getBoolean(col++);
                    String propertyClass = rs.getString(col++);


                    //because of past mistakes, we have Discovery->Enterprise mappings for patients that
                    //shouldn't, so we also need to check that the service ID is definitely a publisher to this subscriber
                    Boolean isPublisher = hmPermittedPublishers.get(serviceId);
                    if (isPublisher == null) {

                        List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId, null); //passing null means don't filter on system ID
                        for (LibraryItem libraryItem : libraryItems) {
                            Protocol protocol = libraryItem.getProtocol();
                            if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
                                continue;
                            }

                            //check to make sure that this service is actually a PUBLISHER to this protocol
                            boolean isProtocolPublisher = false;
                            for (ServiceContract serviceContract : protocol.getServiceContract()) {
                                if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                                        && serviceContract.getService().getUuid().equals(serviceId)
                                        && serviceContract.getActive() == ServiceContractActive.TRUE) {

                                    isProtocolPublisher = true;
                                    break;
                                }
                            }
                            if (!isProtocolPublisher) {
                                continue;
                            }

                            //check to see if this subscriber config is a subscriber to this DB
                            for (ServiceContract serviceContract : protocol.getServiceContract()) {
                                if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
                                        && serviceContract.getActive() == ServiceContractActive.TRUE) {

                                    ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
                                    UUID subscriberServiceId = UUID.fromString(serviceContract.getService().getUuid());
                                    UUID subscriberTechnicalInterfaceId = UUID.fromString(serviceContract.getTechnicalInterface().getUuid());
                                    org.endeavourhealth.core.database.dal.admin.models.Service subscriberService = serviceRepository.getById(subscriberServiceId);
                                    List<ServiceInterfaceEndpoint> serviceEndpoints = subscriberService.getEndpointsList();
                                    for (ServiceInterfaceEndpoint serviceEndpoint : serviceEndpoints) {
                                        if (serviceEndpoint.getTechnicalInterfaceUuid().equals(subscriberTechnicalInterfaceId)) {
                                            String protocolSubscriberConfigName = serviceEndpoint.getEndpoint();
                                            if (protocolSubscriberConfigName.equals(subscriberConfigName)) {
                                                isPublisher = new Boolean(true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isPublisher == null) {
                            isPublisher = new Boolean(false);
                        }

                        hmPermittedPublishers.put(serviceId, isPublisher);
                    }

                    if (specificPatientId != null) {
                        LOG.debug("Org is publisher = " + isPublisher);
                    }

                    if (!isPublisher.booleanValue()) {
                        continue;
                    }

                    //check if patient ID already exists in the subscriber DB
                    Long subscriberPatientId = enterpriseIdDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), patientId);

                    if (specificPatientId != null) {
                        LOG.debug("Got patient " + patientId + " with UPRN " + uprn + " and property class " + propertyClass + " and subscriber patient ID " + subscriberPatientId);
                    }

                    //if the patient doesn't exist on this subscriber DB, then don't transform this record
                    if (subscriberPatientId == null) {
                        continue;
                    }

                    //see if the patient actually exists in the subscriber DB (might not if the patient is deleted or confidential)
                    String checkSql = "SELECT id FROM patient WHERE id = ?";
                    Connection subscriberConnection2 = connectionWrapper.getConnection();
                    PreparedStatement psCheck = subscriberConnection2.prepareStatement(checkSql);
                    psCheck.setLong(1, subscriberPatientId);
                    ResultSet checkRs = psCheck.executeQuery();
                    boolean inSubscriberDb = checkRs.next();
                    psCheck.close();
                    subscriberConnection2.close();
                    if (!inSubscriberDb) {
                        LOG.info("Skipping patient " + patientId + " -> " + subscriberPatientId + " as not found in enterprise DB");
                        continue;
                    }

                    SubscriberOrgMappingDalI orgMappingDal = DalProvider.factorySubscriberOrgMappingDal(subscriberConfigName);
                    Long subscriberOrgId = orgMappingDal.findEnterpriseOrganisationId(serviceId);

                    String discoveryPersonId = patientLinkDal.getPersonId(patientId);
                    SubscriberPersonMappingDalI personMappingDal = DalProvider.factorySubscriberPersonMappingDal(subscriberConfigName);
                    Long subscriberPersonId = personMappingDal.findOrCreateEnterprisePersonId(discoveryPersonId);

                    String lsoaCode = null;
                    if (!Strings.isNullOrEmpty(abpAddress)) {
                        String[] toks = abpAddress.split(" ");
                        String postcode = toks[toks.length - 1];
                        PostcodeLookup postcodeReference = postcodeDal.getPostcodeReference(postcode);
                        if (postcodeReference != null) {
                            lsoaCode = postcodeReference.getLsoaCode();
                        }
                    }


                    col = 1;
                    psUpsert.setLong(col++, subscriberPatientId);
                    psUpsert.setLong(col++, subscriberOrgId);
                    psUpsert.setLong(col++, subscriberPersonId);
                    psUpsert.setString(col++, lsoaCode);

                    if (pseudonymised) {

                        String pseuoUprn = null;
                        if (uprn != null) {

                            TreeMap<String, String> keys = new TreeMap<>();
                            keys.put("UPRN", "" + uprn);

                            Crypto crypto = new Crypto();
                            crypto.SetEncryptedSalt(saltBytes);
                            pseuoUprn = crypto.GetDigest(keys);
                        }

                        psUpsert.setString(col++, pseuoUprn);
                    } else {
                        if (uprn != null) {

                            psUpsert.setLong(col++, uprn.longValue());
                        } else {
                            psUpsert.setNull(col++, Types.BIGINT);
                        }
                    }
                    psUpsert.setString(col++, qualifier);
                    psUpsert.setString(col++, algorithm);
                    psUpsert.setString(col++, match);
                    psUpsert.setBoolean(col++, noAddress);
                    psUpsert.setBoolean(col++, invalidAddress);
                    psUpsert.setBoolean(col++, missingPostcode);
                    psUpsert.setBoolean(col++, invalidPostcode);
                    psUpsert.setString(col++, propertyClass);

                    if (specificPatientId != null) {
                        LOG.debug("" + psUpsert);
                    }

                    psUpsert.addBatch();
                    inBatch++;
                    saved++;

                    if (inBatch >= saveBatchSize) {
                        try {
                            psUpsert.executeBatch();
                            subscriberConnection.commit();
                            inBatch = 0;
                        } catch (Exception ex) {
                            LOG.error("Error saving UPRN for " + patientId + " -> " + subscriberPatientId + " for org " + subscriberOrgId);
                            LOG.error("" + psUpsert);
                            throw ex;
                        }
                    }

                    checked++;
                    if (checked % 1000 == 0) {
                        LOG.info("Checked " + checked + " Saved " + saved);
                    }
                }

                if (inBatch > 0) {
                    psUpsert.executeBatch();
                    subscriberConnection.commit();
                }

                LOG.info("Chcked " + checked + " Saved " + saved);

                psUpsert.close();

                subscriberConnection.close();
                edsEntityManager.close();

                subscriberConnection.close();
            }

            LOG.info("Finished Populating Subscriber UPRN Table for " + subscriberConfigName);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    public static void bulkProcessUPRN(String subscriberConfigName, String protocolName, String outputFormat, String filePath, String debug) throws Exception {

        Set<UUID> hsPatientUuids = new HashSet<>();
        Set<UUID> hsServiceUuids = new HashSet<>();
        File f = new File(filePath);
        if (f.exists()) {
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
        }

        LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

        if (matchedLibraryItem == null) {
            LOG.debug("Protocol not found : " + protocolName);
            return;
        }
        List<ServiceContract> l = matchedLibraryItem.getProtocol().getServiceContract();
        String serviceId = "";
        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

        //List<ResourceWrapper> resources = new ArrayList<>();
        for (ServiceContract serviceContract : l) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {


                UUID batchUUID = UUID.randomUUID();
                serviceId = serviceContract.getService().getUuid();
                UUID serviceUUID = UUID.fromString(serviceId);

                if (hsServiceUuids.contains(serviceUUID)) {
                    // already processed the service so skip it entirely
                    continue;
                }

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID, true);
                for (UUID patientId : patientIds) {

                    // check if we have processed the patient already
                    if (hsPatientUuids.contains(patientId)) {
                        continue;
                    }
                    List<String> newLines = new ArrayList<>();
                    newLines.add(patientId.toString());
                    Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                    //resources.clear();
                    List<ResourceWrapper> resources = new ArrayList<>();

                    ResourceWrapper patientWrapper = dal.getCurrentVersion(serviceUUID, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null) {
                        LOG.warn("Null patient resource for Patient " + patientId);
                        continue;
                    }

                    resources.add(patientWrapper);

                    if (debug.equals("1")) {
                        LOG.info("Service: " + serviceUUID.toString());
                        LOG.info("Configname: " + subscriberConfigName);
                        LOG.info("Patientid: " + patientId.toString());
                        //LOG.info("resources: " + resources.toString());
                        //System.out.println("Press Enter key to continue...");
                        //Scanner scan = new Scanner(System.in);
                        //scan.nextLine();
                    }

                    if (outputFormat.equals("SUBSCRIBER")) {

                        String containerString = BulkHelper.getSubscriberContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId);

                        //  Is a random UUID ok to use as a queued message ID
                        if (containerString != null) {
                            org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
                        }

                    } else {
                        String containerString = BulkHelper.getEnterpriseContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId, debug);

                        //  Is a random UUID ok to use as a queued message ID
                        if (containerString != null) {
                            EnterpriseFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
                        }
                    }
                }
                hsServiceUuids.add(serviceUUID);
            }
        }
    }


    public static void bulkProcessUPRNThreaded(String subscriberConfigName, String protocolName, String outputFormat, String filePath, String debug, Integer threads, Integer QBeforeBlock) throws Exception {

        Set<UUID> hsPatientUuids = new HashSet<>();
        Set<UUID> hsServiceUuids = new HashSet<>();
        File f = new File(filePath);
        if (f.exists()) {
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
        }

        LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

        if (matchedLibraryItem == null) {
            LOG.debug("Protocol not found : " + protocolName);
            return;
        }
        List<ServiceContract> l = matchedLibraryItem.getProtocol().getServiceContract();
        String serviceId = "";
        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();

        SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

        ThreadPool threadPool = new ThreadPool(threads, QBeforeBlock);

        Long ret;

        for (ServiceContract serviceContract : l) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                UUID batchUUID = UUID.randomUUID();
                serviceId = serviceContract.getService().getUuid();
                UUID serviceUUID = UUID.fromString(serviceId);

                if (hsServiceUuids.contains(serviceUUID)) {
                    // already processed the service
                    continue;
                }

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID, true);

                for (UUID patientId : patientIds) {

                    // check if we have processed the patient already
                    if (hsPatientUuids.contains(patientId)) {
                        continue;
                    }
                    List<String> newLines = new ArrayList<>();
                    newLines.add(patientId.toString());
                    Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                    LOG.info(patientId.toString());
                    ret = enterpriseIdDal.findEnterpriseIdOldWay("Patient", patientId.toString());
                    if (ret != null) {
                        // check if the patient has previously been processed?
                        LOG.info(ret.toString());
                    }
                    List<ThreadPoolError> errors = threadPool.submit(new UPRNCallable(serviceUUID, ResourceType.Patient.toString(), patientId, debug, dal, outputFormat, subscriberConfigName, batchUUID));
                    //handleErrors(errors);
                }

                hsServiceUuids.add(serviceUUID);
            }
        }

        List<ThreadPoolError> errors = threadPool.waitAndStop();
        //handleErrors(errors);
    }

    public static void bulkProcessUPRNThreadedNewWay(String subscriberConfigName,
                                                     String orgOdsCodeRegex,
                                                     // String protocolName,
                                                     String outputFormat,
                                                     String filePath,
                                                     String debug,
                                                     Integer threads,
                                                     Integer QBeforeBlock) throws Exception {

        Set<UUID> hsPatientUuids = new HashSet<>();
        Set<UUID> hsServiceUuids = new HashSet<>();
        File f = new File(filePath);
        if (f.exists()) {
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
        }

        /*
        LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

        if (matchedLibraryItem == null) {
            LOG.debug("Protocol not found : " + protocolName);
            return;
        }
        List<ServiceContract> l = matchedLibraryItem.getProtocol().getServiceContract();
        String serviceId = "";
        */

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> services = serviceDal.getAll();
        SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
        String bulkOperationName = "Bulk load of patient_address_match and patient_address_ralf tables for " + subscriberConfigName;

        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
        ThreadPool threadPool = new ThreadPool(threads, QBeforeBlock);
        Long ret;

        // for (ServiceContract serviceContract : l) {
        for (Service service : services) {

            /*
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER) && serviceContract.getActive() == ServiceContractActive.TRUE) {

                UUID batchUUID = UUID.randomUUID();
                serviceId = serviceContract.getService().getUuid();
                UUID serviceUUID = UUID.fromString(serviceId);

                if (hsServiceUuids.contains(serviceUUID)) {
                    // already processed the service
                    continue;
                }
             */

            // List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID, true);

            if (shouldSkipService(service, orgOdsCodeRegex)) {
                continue;
            }

            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId(), service.getLocalId());
            if (!subscriberConfigNames.contains(subscriberConfigName)) {
                LOG.debug("Skipping " + service + " as not a publisher");
                continue;
            }

            // check if already done
            if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                LOG.debug("Skipping " + service + " as already done");
                continue;
            }

            UUID batchUUID = UUID.randomUUID();
            LOG.debug("Doing " + service);
            UUID serviceId = service.getId();

            // For bulkProcessUPRNThreaded above, this was done with the boolean set to true in the arguments for the getPatientIds call,
            // But for for bulkProcessUPRNThreadedNewWay, this is done with the boolean set to false in the arguments for that method call
            List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
            LOG.debug("Found " + patientIds.size() + " patients");


            for (UUID patientId : patientIds) {

                // check if in cohort
                SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                if (cohortRecord == null
                        || !cohortRecord.isInCohort()) {
                    continue;
                }

                // check if we have processed the patient already
                if (hsPatientUuids.contains(patientId)) {
                    continue;
                }

                List<String> newLines = new ArrayList<>();
                newLines.add(patientId.toString());
                Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                LOG.info(patientId.toString());
                ret = enterpriseIdDal.findEnterpriseIdOldWay("Patient", patientId.toString());
                if (ret != null) {
                    // check if the patient has previously been processed?
                    LOG.info(ret.toString());
                }
                List<ThreadPoolError> errors = threadPool.submit(new UPRNCallable(serviceId, ResourceType.Patient.toString(), patientId, debug, dal, outputFormat, subscriberConfigName, batchUUID));
                //handleErrors(errors);
            }

            hsServiceUuids.add(serviceId);

            //audit what has been done
            setServiceDoneBulkOperation(service, bulkOperationName);

        }

        List<ThreadPoolError> errors = threadPool.waitAndStop();
        //handleErrors(errors);
    }

    static class UPRNCallable implements Callable {
        private UUID serviceUUID;
        private String ResourceType;
        private UUID patientId;
        private String debug;
        private ResourceDalI dal;
        private String outputFormat;
        private String subscriberConfigName;
        private UUID batchUUID;

        public UPRNCallable(UUID serviceUUID, String ResourceType, UUID patientId, String debug, ResourceDalI dal, String outputFormat, String subscriberConfigName, UUID batchUUID) {
            this.serviceUUID = serviceUUID;
            this.ResourceType = ResourceType;
            this.patientId = patientId;
            this.debug = debug;
            this.dal = dal;
            this.outputFormat = outputFormat;
            this.subscriberConfigName = subscriberConfigName;
            this.batchUUID = batchUUID;
        }

        @Override
        public Object call() throws Exception {

            try {
                List<ResourceWrapper> resources = new ArrayList<>();

                ResourceWrapper patientWrapper = dal.getCurrentVersion(serviceUUID, ResourceType, patientId);

                if (patientWrapper == null) {
                    // LOG.warn("Null patient resource for Patient " + patientId);
                    return null;
                }

                resources.add(patientWrapper);

                if (debug.equals("1")) {
                    LOG.info("Service: " + serviceUUID.toString());
                    LOG.info("Configname: " + subscriberConfigName);
                    LOG.info("Patientid: " + patientId.toString());
                }

                if (outputFormat.equals("SUBSCRIBER")) {

                    String containerString = BulkHelper.getSubscriberContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId);

                    //  Is a random UUID ok to use as a queued message ID
                    if (containerString != null) {
                        org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
                    }

                } else {
                    String containerString = BulkHelper.getEnterpriseContainerForUPRNData(resources, serviceUUID, batchUUID, subscriberConfigName, patientId, debug);

                    //  Is a random UUID ok to use as a queued message ID
                    if (containerString != null) {
                        EnterpriseFiler.file(batchUUID, UUID.randomUUID(), containerString, subscriberConfigName);
                    }
                }
                return null;
            } catch (Exception e) {
                LOG.error(e.toString());
            }
            return null;
        }
    }

}
