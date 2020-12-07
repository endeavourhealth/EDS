package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMappingAudit;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class Fhir extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(Fhir.class);

    /**
     * finds all raw data that was used to generate a specific record from a subscriber ID
     */
    public static void dumpRawDataForSubscriberId(String subscriberConfigName, long subscriberId) {
        LOG.debug("Dumping Raw Data for Subscriber " + subscriberConfigName + " and Record " + subscriberId);
        try {

            //first task is to find the resource_type and resource_id
            String resourceType = null;
            UUID resourceId = null;

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
            Connection connection = ConnectionManager.getSubscriberTransformConnection(subscriberConfigName);
            Statement statement = connection.createStatement();
            String sql = null;
            ResultSet rs = null;

            //if compass v1, then it uses a mix of the old enterprise_id_map tables AND the newer subscriber_id_map tables, so we need to check both
            if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {

                sql = "SELECT resource_type, resource_id FROM enterprise_id_map_3 WHERE enterprise_id = " + subscriberId
                    + " UNION SELECT resource_type, resource_id FROM enterprise_id_map WHERE enterprise_id = " + subscriberId;
                rs = statement.executeQuery(sql);
                if (rs.next()) {
                    LOG.debug("Found ID in enterprise map table");
                    resourceType = rs.getString(1);
                    resourceId = UUID.fromString(rs.getString(2));

                }
            }

            if (resourceType == null) {
                sql = "SELECT source_id FROM subscriber_id_map WHERE subscriber_id = " + subscriberId
                    + " UNION SELECT source_id FROM subscriber_id_map_3 WHERE subscriber_id = " + subscriberId;
                rs = statement.executeQuery(sql);
                if (rs.next()) {
                    LOG.debug("Found ID in subscriber map table");
                    String sourceId = rs.getString(1);
                    int index = sourceId.indexOf("/");
                    if (index == -1) {
                        throw new Exception("Unexpected source ID format [" + sourceId + "]");
                    }
                    resourceType = sourceId.substring(0, index);
                    String suffix = sourceId.substring(index+1);
                    if (suffix.length() < 36) {
                        throw new Exception("Unexpected suffix length in source ID [" + sourceId + "]");
                    }
                    suffix = suffix.substring(0, 36);
                    resourceId = UUID.fromString(suffix);
                }
            }

            statement.close();
            connection.close();

            if (resourceType == null) {
                throw new Exception("Failed to find subscriber ID " + subscriberId + " in subscriber_transform database for " + subscriberConfigName);
            }

            dumpRawDataForResourceIdImpl(resourceType, resourceId);

            LOG.debug("Finished Dumping Raw Data for Subscriber " + subscriberConfigName + " and Record " + subscriberId);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void dumpRawDataForResourceId(String resourceType, UUID resourceId) {
        LOG.debug("Dumping Raw Data for Resource " + resourceType + " " + resourceId);
        try {
            dumpRawDataForResourceIdImpl(resourceType, resourceId);

            LOG.debug("Finished Dumping Raw Data for Resource " + resourceType + " " + resourceId);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void dumpRawDataForResourceIdImpl(String resourceType, UUID resourceId) throws Exception {

        //we need to find the service ID for the resource, and there's no good way to do this other than test them all
        UUID serviceId = null;
        List<ResourceWrapper> history = null;

        Map<String, Service> publishers = findPublisherConfigNames();
        for (String publisher: publishers.keySet()) {
            Service exampleService = publishers.get(publisher); //we need a service just to use the resource DAL interface

            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            history = resourceDal.getResourceHistory(exampleService.getId(), resourceType, resourceId);
            if (history == null || history.isEmpty()) {
                LOG.debug("Failed to find resource history on " + publisher);

            } else {
                ResourceWrapper first = history.get(0);
                serviceId = first.getServiceId();
                LOG.debug("Found resource history on " + publisher);
                break;
            }
        }

        if (serviceId == null) {
            throw new Exception("Failed to find resource history on any publisher");
        }

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getById(serviceId);
        LOG.debug("Resource belongs to service " + service);
        LOG.debug("Resource history size is " + history.size());

        //find all the mapping documents, and hash by version
        Map<UUID, ResourceFieldMappingAudit> hmAudits = findResourceMappings(serviceId, resourceType, resourceId);

        for (ResourceWrapper wrapper: history) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LOG.debug("Wrapper from " + sdf.format(wrapper.getCreatedAt()));
            if (wrapper.isDeleted()) {
                LOG.debug("DELETED");
            } else {
                LOG.debug(wrapper.getResourceData());
            }
            LOG.debug("");

            UUID version = wrapper.getVersion();
            ResourceFieldMappingAudit audit = hmAudits.get(version);
            if (audit == null) {
                LOG.debug("NO mapping audit found");

            } else {
                Map<Integer, Integer> hmFileIds = new HashMap<>();

                List<ResourceFieldMappingAudit.ResourceFieldMappingAuditRow> auditRows = audit.getAudits();
                for (ResourceFieldMappingAudit.ResourceFieldMappingAuditRow row: auditRows) {
                    if (row.getOldStyleAuditId() != null) {
                        convertOldStyleAuditToNew(serviceId, row.getOldStyleAuditId(), hmFileIds);
                    } else {
                        int fileId = row.getFileId();
                        int record = row.getRecord();
                        hmFileIds.put(new Integer(fileId), new Integer(record));
                    }
                }

                for (Integer fileId: hmFileIds.keySet()) {
                    Integer recordId = hmFileIds.get(fileId);
                    dumpAuditRecord(fileId, recordId);
                    LOG.debug("");
                }
            }

            LOG.debug("");
            LOG.debug("");
        }
    }


    /**
     * converts old-style mappings to new using the lookup table on each publisher_transform DB
     */
    private static void convertOldStyleAuditToNew(UUID serviceId, Long oldStyleAuditId, Map<Integer, Integer> hmNewFileIds) throws Exception {

        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = null;
        try {
            String sql = "SELECT published_file_id, record_number"
                    + " FROM source_file_record_audit"
                    + " WHERE id = ?";
            ps = connection.prepareStatement(sql);

            ps.setLong(1, oldStyleAuditId.longValue());

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Failed to find published file details for old-style audit ID " + oldStyleAuditId);
            }

            int publishedFileId = rs.getInt(1);
            int recordNumber = rs.getInt(2);

            hmNewFileIds.put(new Integer(publishedFileId), new Integer(recordNumber));

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }


    private static void dumpAuditRecord(Integer fileId, Integer recordNumber) throws Exception {

        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT file_path, published_file_type_id"
                    + " FROM published_file"
                    + " WHERE id = ?";
            ps = connection.prepareStatement(sql);
            ps.setInt(1, fileId.intValue());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                LOG.error("Failed to find published_file row for id " + fileId);
                return;
            }

            String filePath = rs.getString(1);
            int fileTypeId = rs.getInt(2);
            ps.close();
            LOG.debug(filePath);
            LOG.debug("Record " + recordNumber);
            LOG.debug("");

            //get the column headers for this file type
            List<String> columnHeaders = new ArrayList<>();

            sql = "SELECT column_name FROM published_file_type_column WHERE published_file_type_id = ? ORDER BY column_index";
            ps = connection.prepareStatement(sql);
            ps.setInt(1, fileTypeId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String col = rs.getString(1);
                columnHeaders.add(col);
            }
            ps.close();
            if (columnHeaders.isEmpty()) {
                LOG.error("Failed to find column headers for file type id " + fileTypeId);
                return;
            }
            String headerStr = "\"" + String.join("\",\"", columnHeaders) + "\"";
            LOG.debug(headerStr);

            //find the file offset
            sql = "SELECT byte_start, byte_length "
                    + " FROM published_file_record"
                    + " WHERE published_file_id = ?"
                    + " AND record_number = ?";
            ps = connection.prepareStatement(sql);
            ps.setInt(1, fileId.intValue());
            ps.setInt(2, recordNumber.intValue());
            rs = ps.executeQuery();
            if (!rs.next()) {
                LOG.error("Failed to find published_file_record for file " + fileId + " and record " + recordNumber);
                return;
            }
            long byteStart = rs.getLong(1);
            int byteLen = rs.getInt(2);

            String line = FileHelper.readCharactersFromSharedStorage(filePath, byteStart, byteLen);
            if (Strings.isNullOrEmpty(line)) {
                LOG.error("Failed to find raw line data in " + filePath + " from " + byteStart + " len " + byteLen);
                return;
            }

            LOG.debug(line);

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static Map<UUID, ResourceFieldMappingAudit> findResourceMappings(UUID serviceId, String resourceType, UUID resourceId) throws Exception {

        Map<UUID, ResourceFieldMappingAudit> ret = new HashMap<>();

        String sql = "SELECT version, mappings_json FROM resource_field_mappings WHERE resource_type = ? AND resource_id = ?";

        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, resourceType);
        ps.setString(2, resourceId.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            UUID version = UUID.fromString(rs.getString(1));
            ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(rs.getString(2));
            ret.put(version, audit);
        }
        ps.close();
        connection.close();

        connection = ConnectionManager.getFhirAuditConnection();
        ps = connection.prepareStatement(sql);
        ps.setString(1, resourceType);
        ps.setString(2, resourceId.toString());
        rs = ps.executeQuery();
        while (rs.next()) {
            UUID version = UUID.fromString(rs.getString(1));
            ResourceFieldMappingAudit audit = ResourceFieldMappingAudit.readFromJson(rs.getString(2));
            ret.put(version, audit);
        }
        ps.close();
        connection.close();

        return ret;
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
}
