package org.endeavourhealth.queuereader.routines;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMappingAudit;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLNonTransientConnectionException;
import java.util.*;

public class SD107x extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD107x.class);

    private static final String BULK_OPERATION_NAME = "Moving FHIR resource mappings (SD-107)";

    /**
     * routine for SD-107
     * moves all FHIR resource mappings from both
     *
     */
    public static void moveFhirResourceMappings(boolean includeStartedButNotFinishedServices, int batchSize, boolean verbose, String odsCodeRegex) {
        LOG.debug("Moving FHIR Resource Mappings for " + odsCodeRegex + " with batch size " + batchSize);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                //check to see if already done this services
                if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                    continue;
                }

                LOG.debug("Doing " + service);
                moveFhirResourceMappingsForService(service, batchSize, verbose);

                //record as done
                setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
            }

            LOG.debug("Finished Moving FHIR Resource Mappings for " + odsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void moveFhirResourceMappingsForService(Service service, int batchSize, boolean verbose) throws Exception {

        movePatientFhirResourceMappingsForService(service, batchSize, verbose);
        moveAdminFhirResourceMappingsForService(service, batchSize, verbose);
    }

    private static void moveAdminFhirResourceMappingsForService(Service service, int batchSize, boolean verbose) throws Exception {

        LOG.debug("Moving admin resource mappings");
        moveFhirResourceMappingsForServiceAndPatient(service.getId(), null, batchSize, verbose);
        LOG.debug("Done admin resource mappings");
    }

    private static void movePatientFhirResourceMappingsForService(Service service, int batchSize, boolean verbose) throws Exception {

        //find all patient UUIDs
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(service.getId(), true);
        LOG.debug("Found " + patientIds.size() + " patient IDs");

        int done = 0;
        for (UUID patientId: patientIds) {

            moveFhirResourceMappingsForServiceAndPatient(service.getId(), patientId, batchSize, verbose);

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " of " + patientIds.size());
            }
        }
        LOG.debug("Done " + done + " of " + patientIds.size());

    }

    private static void moveFhirResourceMappingsForServiceAndPatient(UUID serviceId, UUID patientId, int batchSize, boolean verbose) throws Exception {

        if (verbose) {
            LOG.trace("Doing patient " + patientId);
        }

        Connection connection = null;
        try {
            connection = ConnectionManager.getEhrNonPooledConnection(serviceId);
        } catch (Exception ex) {
            if (ex.getMessage().contains("Unknown publisher config name for service")) {
                //if we get the above message it's one of the handful of publishers where we've not received any data yet
                return;
            } else {
                throw ex;
            }
        }

        PreparedStatement ps = null;
        try {
            String sql = "SELECT resource_id, resource_type FROM resource_current WHERE service_id = ? AND patient_id = ?";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, serviceId.toString());
            if (patientId != null) {
                ps.setString(col++, patientId.toString());
            } else {
                //resource_current uses an empty string for admin resources
                ps.setString(col++, "");
                //ps.setNull(col++, Types.VARCHAR);
            }

            int doneResources = 0;
            List<String> batch = new ArrayList<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                String resourceId = rs.getString(col++);
                String resourceType = rs.getString(col++);

                String ref = ReferenceHelper.createResourceReference(resourceType, resourceId);
                batch.add(ref);

                if (batch.size() >= batchSize) {
                    moveFhirResourceMappingsForResources(serviceId, batch, verbose);
                    batch.clear();
                }

                doneResources ++;
                if (doneResources % 100 == 0 && verbose) {
                    LOG.trace("Done " + doneResources + " resoureces for patient " + patientId);
                }
            }

            if (!batch.isEmpty()) {
                moveFhirResourceMappingsForResources(serviceId, batch, verbose);
                batch.clear();
            }

            if (verbose) {
                LOG.trace("Finished " + doneResources + " resoureces for patient " + patientId);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }

            try {
                connection.close();
            } catch (SQLNonTransientConnectionException ex) {
                //we've had exceptions here a few times, but don't fail
            }
        }

    }

    private static void moveFhirResourceMappingsForResources(UUID serviceId, List<String> batch, boolean verbose) throws Exception {

        Map<String, List<AuditWrapper>> mappings = new HashMap<>();

        //get mappings from publisher_transform
        if (verbose) {
            LOG.trace("Retrieving from publisher_transform");
        }
        retrieveMappingsFromTable(serviceId, batch, true, mappings);

        //get mappings from core_management
        if (verbose) {
            LOG.trace("Retrieving from fhir_audit");
        }
        retrieveMappingsFromTable(serviceId, batch, false, mappings);

        //fix any old-style mappings
        if (verbose) {
            LOG.trace("Fixing old-style mappings");
        }
        fixOldStyleMappings(serviceId, mappings);

        //write to new table
        if (verbose) {
            LOG.trace("Writing to new table");
        }
        writeMappingsForBatch(serviceId, batch, mappings);

        //delete from publisher_transform
        if (verbose) {
            LOG.trace("Deleting from publisher_transform");
        }
        deleteMappingsFromTable(serviceId, batch, true);

        //delete from core_management
        if (verbose) {
            LOG.trace("Deleting from fhir_audit");
        }
        deleteMappingsFromTable(serviceId, batch, false);
    }

    /*private static void moveFhirResourceMappingsForResource(UUID serviceId, UUID resourceId, String resourceType) throws Exception {

        //get mappings from publisher_transform
        List<AuditWrapper> ptMappings = retrieveMappingsFromTable(serviceId, resourceId, resourceType, true);

        //get mappings from core_management
        List<AuditWrapper> cmMappings = retrieveMappingsFromTable(serviceId, resourceId, resourceType, false);

        List<AuditWrapper> allMappings = new ArrayList<>();
        allMappings.addAll(ptMappings);
        allMappings.addAll(cmMappings);

        //fix any old-style mappings
        fixOldStyleMappings(serviceId, allMappings);

        //write to new table
        writeMappingsForResource(serviceId, resourceId, resourceType, allMappings);

        //delete from publisher_transform
        deleteMappingsFromTable(serviceId, resourceId, resourceType, true);

        //delete from core_management
        deleteMappingsFromTable(serviceId, resourceId, resourceType, false);
    }*/

    /*private static void fixOldStyleMappings(UUID serviceId, List<AuditWrapper> allMappings) throws Exception {

        for (AuditWrapper wrapper: allMappings) {
            ResourceFieldMappingAudit audit = wrapper.getAudit();

            for (ResourceFieldMappingAudit.ResourceFieldMappingAuditRow row: audit.getAudits()) {

                //if it's got this old-style audit ID, then we need to convert to the new-style
                if (row.getOldStyleAuditId() != null) {
                    //if we've got an unconverted old-style audit, then we can look up the new-style details
                    //in the mapping table on each database
                    convertOldStyleAuditToNew(row, serviceId);
                }
            }
        }
    }*/

    private static void fixOldStyleMappings(UUID serviceId, Map<String, List<AuditWrapper>> hmMappings) throws Exception {

        List<String> mappingsToRemove = new ArrayList<>();

        for (String ref: hmMappings.keySet()) {
            List<AuditWrapper> mappings = hmMappings.get(ref);

            for (AuditWrapper wrapper: mappings) {
                ResourceFieldMappingAudit audit = wrapper.getAudit();

                for (ResourceFieldMappingAudit.ResourceFieldMappingAuditRow row : audit.getAudits()) {

                    //if it's got this old-style audit ID, then we need to convert to the new-style
                    if (row.getOldStyleAuditId() != null) {
                        //if we've got an unconverted old-style audit, then we can look up the new-style details
                        //in the mapping table on each database
                        try {
                            convertOldStyleAuditToNew(row, serviceId);

                        } catch (Exception ex) {
                            //we just can't recover
                            LOG.error("Failed to convert old-style mapping, so will remove", ex);
                            mappingsToRemove.add(ref);
                            break;
                            //throw new Exception("Failed to convert old-style audit to new for " + ref, ex);
                        }
                    }
                }
            }
        }

        //remove any we couldn't convert
        for (String ref: mappingsToRemove) {
            hmMappings.remove(ref);
        }
    }

    private static void convertOldStyleAuditToNew(ResourceFieldMappingAudit.ResourceFieldMappingAuditRow row, UUID serviceId) throws Exception {
        Long oldSourceRecordId = row.getOldStyleAuditId();

        int[] ints = findNewStyleAuditsOnPublisherTransform(serviceId, oldSourceRecordId.longValue());
        if (ints == null) {
            LOG.debug("Failed to find lookup for old-style audit " + oldSourceRecordId);

            //if we failed to find a mapping, then we can look on other publisher_transform DBs. There was a previous issue
            //with how the Emis drug and clinical codes were audited, where we could have a mapping audit linked to the file record audit
            //on another publisher_transform DB
            Map<String, Service> publishers = findPublisherConfigNames();
            for (String publisher: publishers.keySet()) {
                Service exampleService = publishers.get(publisher); //we need a service just to use the resource DAL interface
                ints = findNewStyleAuditsOnPublisherTransform(exampleService.getId(), oldSourceRecordId.longValue());

                //if a mapping can be found on that publisher transform, verify that it's for a drug or clinical code file
                if (ints != null) {
                    int publishedFileId = ints[0];
                    String filePath = findFilePathForPublishedFileId(publishedFileId);
                    LOG.debug("Did find audit on " + publisher + " for file ID " + publishedFileId + " -> " + FilenameUtils.getName(filePath));
                    if (filePath.contains("ClinicalCode")
                            || filePath.contains("DrugCode")) {
                        LOG.debug("Using this file ID");
                        break;
                    }
                }
            }

            //if we've still not got a mapping, then throw an error
            if (ints == null) {
                throw new Exception("Failed to find published file details for old-style audit ID " + oldSourceRecordId);
            }
        }

        int publishedFileId = ints[0];
        int recordNumber = ints[1];

        row.setFileId(publishedFileId);
        row.setRecord(recordNumber);
        row.setOldStyleAuditId(null);
    }

    private static String findFilePathForPublishedFileId(int publishedFileId) throws Exception {
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT file_path FROM published_file WHERE id = ?";
            ps = connection.prepareStatement(sql);

            ps.setInt(1, publishedFileId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                throw new Exception("Failed to find file path for file ID " + publishedFileId);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static int[] findNewStyleAuditsOnPublisherTransform(UUID serviceId, long oldSourceRecordId) throws Exception {
        EntityManager entityManager = ConnectionManager.getPublisherTransformEntityManager(serviceId);
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl)entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = "SELECT record_number, published_file_id"
                    + " FROM source_file_record_audit"
                    + " WHERE id = ?";
            ps = connection.prepareStatement(sql);

            ps.setLong(1, oldSourceRecordId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int recordNumber = rs.getInt(1);
                if (rs.wasNull()) {
                    return null;
                }
                int publishedFileId = rs.getInt(2);
                if (rs.wasNull()) { //got some odd records with null file ID
                    return null;
                }
                return new int[] {publishedFileId, recordNumber};
            }

            return null;

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }
    }

    private static Map<String, Service> findPublisherConfigNames() throws Exception {

        Map<String, Service> ret = new HashMap<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> services = serviceDal.getAll();
        for (Service service: services) {
            String publisher = service.getPublisherConfigName();
            if (!Strings.isNullOrEmpty(publisher)) {
                ret.put(publisher, service);
            }
        }

        return ret;
    }

    private static void writeMappingsForBatch(UUID serviceId, List<String> batch, Map<String, List<AuditWrapper>> hmMappings) throws Exception {

        Connection connection = getConnection(serviceId, true);
        PreparedStatement ps = null;
        try {

            String sql = "INSERT INTO fhir_mapping_audit"
                    + " (service_id, resource_id, resource_type, version, created_at, mappings_json)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";

            ps = connection.prepareStatement(sql);

            for (String ref: hmMappings.keySet()) {

                ReferenceComponents comps = ReferenceHelper.getReferenceComponents(ReferenceHelper.createReference(ref));
                List<AuditWrapper> mappings = hmMappings.get(ref);

                for (AuditWrapper wrapper : mappings) {
                    ResourceFieldMappingAudit audit = wrapper.getAudit();
                    String mappingJson = audit.writeToJson();

                    int col = 1;
                    ps.setString(col++, serviceId.toString());
                    ps.setString(col++, comps.getId());
                    ps.setString(col++, comps.getResourceType().toString());
                    ps.setString(col++, wrapper.getVersion().toString());
                    ps.setTimestamp(col++, new java.sql.Timestamp(wrapper.getCreated().getTime()));
                    ps.setString(col++, mappingJson);

                    ps.addBatch();
                }
            }

            ps.executeBatch();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw new Exception("Failed to insert mappings for " + batch, ex);

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /*private static void writeMappingsForResource(UUID serviceId, UUID resourceId, String resourceType, List<AuditWrapper> allMappings) throws Exception {

        Connection connection = getConnection(serviceId, true);
        PreparedStatement ps = null;
        try {

            String sql = "INSERT INTO fhir_mapping_audit"
                    + " (service_id, resource_id, resource_type, version, created_at, mappings_json)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";

            ps = connection.prepareStatement(sql);

            for (AuditWrapper wrapper: allMappings) {
                ResourceFieldMappingAudit audit = wrapper.getAudit();
                String mappingJson = audit.writeToJson();

                int col = 1;
                ps.setString(col++, serviceId.toString());
                ps.setString(col++, resourceId.toString());
                ps.setString(col++, resourceType);
                ps.setString(col++, wrapper.getVersion().toString());
                ps.setTimestamp(col++, new java.sql.Timestamp(wrapper.getCreated().getTime()));
                ps.setString(col++, mappingJson);

                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw new Exception("Failed to insert mappings for " + resourceId + "/" + resourceType, ex);

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }*/

    private static void deleteMappingsFromTable(UUID serviceId, List<String> batch, boolean publisherTransform) throws Exception {

        Connection connection = getConnection(serviceId, publisherTransform);
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM resource_field_mappings" +
                    " WHERE ";
            for (int i=0; i<batch.size(); i++) {
                if (i>0) {
                    sql += " OR ";
                }
                sql += "(resource_type = ? AND resource_id = ?)";
            }
            ps = connection.prepareStatement(sql);

            int col = 1;
            for (String ref: batch) {
                ReferenceComponents comps = ReferenceHelper.getReferenceComponents(ReferenceHelper.createReference(ref));
                ps.setString(col++, comps.getResourceType().toString());
                ps.setString(col++, comps.getId());
            }

            ps.executeUpdate();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw new Exception("Failed to delete mappings for " + batch + " publisherTransform = " + publisherTransform, ex);

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static Connection getConnection(UUID serviceId, boolean publisherTransform) throws Exception {
        if (publisherTransform) {
            return ConnectionManager.getPublisherTransformConnection(serviceId);
        } else {
            return ConnectionManager.getFhirAuditConnection();
        }
    }

    private static void retrieveMappingsFromTable(UUID serviceId, List<String> batch, boolean publisherTransform, Map<String, List<AuditWrapper>> mappings) throws Exception {

        Connection connection = getConnection(serviceId, publisherTransform);
        PreparedStatement ps = null;
        try {
            String sql = "SELECT resource_type, resource_id, created_at, version, mappings_json" +
                    " FROM resource_field_mappings" +
                    " WHERE ";
            for (int i=0; i<batch.size(); i++) {
                if (i>0) {
                    sql += " OR ";
                }
                sql += "(resource_type = ? AND resource_id = ?)";
            }
            ps = connection.prepareStatement(sql);

            int col = 1;
            for (String ref: batch) {
                ReferenceComponents comps = ReferenceHelper.getReferenceComponents(ReferenceHelper.createReference(ref));
                ps.setString(col++, comps.getResourceType().toString());
                ps.setString(col++, comps.getId());
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                String resourceType = rs.getString(col++);
                String resourceId = rs.getString(col++);
                java.util.Date createdAt = new java.util.Date(rs.getTimestamp(col++).getTime());
                UUID version = UUID.fromString(rs.getString(col++));
                String mappingsJson = rs.getString(col++);

                try {
                    ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(mappingsJson);

                    AuditWrapper w = new AuditWrapper(audit, version, createdAt);
                    String ref = ReferenceHelper.createResourceReference(resourceType, resourceId);

                    List<AuditWrapper> l = mappings.get(ref);
                    if (l == null) {
                        l = new ArrayList<>();
                        mappings.put(ref, l);
                    }
                    l.add(w);
                } catch (JsonMappingException|JsonParseException ex) {
                    //got some unrecoverable entries, so just leave them
                    LOG.error("Error deserialising JSON for " + resourceType + " " + resourceId + " and version " + version, ex);
                }
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /*private static List<AuditWrapper> retrieveMappingsFromTable(UUID serviceId, UUID resourceId, String resourceType, boolean publisherTransform) throws Exception {

        Connection connection = getConnection(serviceId, publisherTransform);
        PreparedStatement ps = null;
        try {
            String sql = "SELECT created_at, version, mappings_json" +
                    " FROM resource_field_mappings" +
                    " WHERE resource_id = ?" +
                    " AND resource_type = ?";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, resourceId.toString());
            ps.setString(col++, resourceType.toString());

            List<AuditWrapper> ret = new ArrayList<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                java.util.Date createdAt = new java.util.Date(rs.getTimestamp(col++).getTime());
                UUID version = UUID.fromString(rs.getString(col++));
                String mappingsJson = rs.getString(col++);
                ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(mappingsJson);

                AuditWrapper w = new AuditWrapper(audit, version, createdAt);
                ret.add(w);
            }

            return ret;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }*/

    /**
     * routine to test that the specific S3 bucket can be written to and read from before I add the path to the configuration proper
     */
    public static void testS3Permissions(String bucketPath) {
        LOG.info("Testing S3 Permissions for " + bucketPath);
        try {
            String host = MetricsHelper.getHostName();
            String fileName = "S3_test_" + host + ".txt";
            String path = FilenameUtils.concat(bucketPath, fileName);

            String body = UUID.randomUUID().toString();
            byte[] bodyBytes = body.getBytes(Charset.defaultCharset());

            //test if exists
            LOG.debug("Doing Exists Test");
            boolean exists = FileHelper.fileExists(path);
            LOG.debug("Done Exists Test " + exists);

            //test write
            LOG.debug("Doing write to " + path);
            FileHelper.writeBytesToSharedStorage(path, bodyBytes);
            LOG.debug("Done write");

            //test read
            LOG.debug("Doing read");
            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(path, Charset.defaultCharset());
            BufferedReader br = new BufferedReader(isr);
            String read = br.readLine();
            br.close();
            LOG.debug("Done read");

            if (read.equals(body)) {
                LOG.debug("Body is same [" + body + "]");
            } else {
                LOG.error("Body is NOT same [" + body + "] vs [" + read + "]");
            }

            //test overwrite
            body = UUID.randomUUID().toString();
            bodyBytes = body.getBytes(Charset.defaultCharset());

            LOG.debug("Doing overwrite to " + path);
            FileHelper.writeBytesToSharedStorage(path, bodyBytes);
            LOG.debug("Done overwrite");

            //test read again
            LOG.debug("Doing read 2");
            isr = FileHelper.readFileReaderFromSharedStorage(path, Charset.defaultCharset());
            br = new BufferedReader(isr);
            read = br.readLine();
            br.close();
            LOG.debug("Done read");

            if (read.equals(body)) {
                LOG.debug("Body is same [" + body + "]");
            } else {
                LOG.error("Body is NOT same [" + body + "] vs [" + read + "]");
            }

            //test if exists
            LOG.debug("Doing Exists Test");
            exists = FileHelper.fileExists(path);
            LOG.debug("Done Exists Test " + exists);

            LOG.info("Testing S3 Permissions for " + bucketPath);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    static class AuditWrapper {
        private ResourceFieldMappingAudit audit = null;
        private UUID version = null;
        private Date created = null;

        public AuditWrapper(ResourceFieldMappingAudit audit, UUID version, Date created) {
            this.audit = audit;
            this.version = version;
            this.created = created;
        }

        public ResourceFieldMappingAudit getAudit() {
            return audit;
        }

        public UUID getVersion() {
            return version;
        }

        public Date getCreated() {
            return created;
        }
    }
}