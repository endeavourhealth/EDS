package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.ResourceAccessor;
import org.endeavourhealth.core.data.ehr.accessors.ResourceHistoryAccessor;
import org.endeavourhealth.core.data.ehr.models.*;
import org.endeavourhealth.core.fhirStorage.metadata.ResourceMetadata;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ResourceRepository extends Repository {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRepository.class);

    public void save(ResourceEntry resourceEntry){
        save(resourceEntry, null, null);
    }

    public void save(ResourceEntry resourceEntry, UUID exchangeId, UUID batchId){
        if (resourceEntry == null) throw new IllegalArgumentException("resourceEntry is null");

        ResourceHistory resourceHistory = new ResourceHistory();
        resourceHistory.setResourceId(resourceEntry.getResourceId());
        resourceHistory.setResourceType(resourceEntry.getResourceType());
        resourceHistory.setVersion(resourceEntry.getVersion());
        resourceHistory.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistory.setServiceId(resourceEntry.getServiceId());
        resourceHistory.setSystemId(resourceEntry.getSystemId());
        resourceHistory.setIsDeleted(false);
        resourceHistory.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceHistory.setResourceData(resourceEntry.getResourceData());
        resourceHistory.setResourceChecksum(resourceEntry.getResourceChecksum());
        Mapper<ResourceHistory> mapperResourceHistory = getMappingManager().mapper(ResourceHistory.class);
        mapperResourceHistory.save(resourceHistory);

        /*ResourceHistoryByService resourceHistoryByService = new ResourceHistoryByService();
        resourceHistoryByService.setResourceId(resourceEntry.getResourceId());
        resourceHistoryByService.setResourceType(resourceEntry.getResourceType());
        resourceHistoryByService.setVersion(resourceEntry.getVersion());
        resourceHistoryByService.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistoryByService.setServiceId(resourceEntry.getServiceId());
        resourceHistoryByService.setSystemId(resourceEntry.getSystemId());
        resourceHistoryByService.setIsDeleted(false);
        resourceHistoryByService.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceHistoryByService.setResourceData(resourceEntry.getResourceData());
        Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        mapperResourceHistoryByService.save(resourceHistoryByService);

        ResourceByService resourceByService = new ResourceByService();
        resourceByService.setServiceId(resourceEntry.getServiceId());
        resourceByService.setSystemId(resourceEntry.getSystemId());
        resourceByService.setResourceType(resourceEntry.getResourceType());
        resourceByService.setResourceId(resourceEntry.getResourceId());
        resourceByService.setCurrentVersion(resourceEntry.getVersion());
        resourceByService.setUpdatedAt(resourceEntry.getCreatedAt());
        resourceByService.setPatientId(resourceEntry.getPatientId());
        resourceByService.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceByService.setResourceMetadata(resourceEntry.getResourceMetadata());
        resourceByService.setResourceData(resourceEntry.getResourceData());
        Mapper<ResourceByService> mapperResourceByService = getMappingManager().mapper(ResourceByService.class);
        mapperResourceByService.save(resourceByService);*/

        //test manual insert rather than materialised view
        /*if (resourceEntry.getPatientId() != null) {
            ResourceByPatient resourceByPatient = new ResourceByPatient();
            resourceByPatient.setServiceId(resourceEntry.getServiceId());
            resourceByPatient.setSystemId(resourceEntry.getSystemId());
            resourceByPatient.setPatientId(resourceEntry.getPatientId());
            resourceByPatient.setResourceType(resourceEntry.getResourceType());
            resourceByPatient.setResourceId(resourceEntry.getResourceId());
            resourceByPatient.setSchemaVersion(resourceEntry.getSchemaVersion());
            resourceByPatient.setResourceMetadata(resourceEntry.getResourceMetadata());
            resourceByPatient.setResourceData(resourceEntry.getResourceData());
            Mapper<ResourceByPatient> mapperResourceByPatient = getMappingManager().mapper(ResourceByPatient.class);
            mapperResourceByPatient.save(resourceByPatient);
        }*/

        if (exchangeId != null && batchId != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(batchId);
            resourceByExchangeBatch.setExchangeId(exchangeId);
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(false);
            resourceByExchangeBatch.setSchemaVersion(resourceEntry.getSchemaVersion());
            resourceByExchangeBatch.setResourceData(resourceEntry.getResourceData());
            Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = getMappingManager().mapper(ResourceByExchangeBatch.class);
            mapperResourceByExchangeBatch.save(resourceByExchangeBatch);
        }
    }
    /*public void save(ResourceEntry resourceEntry, UUID exchangeId, UUID batchId){
        if (resourceEntry == null) throw new IllegalArgumentException("resourceEntry is null");

        BatchStatement batch = new BatchStatement();

        ResourceHistory resourceHistory = new ResourceHistory();
        resourceHistory.setResourceId(resourceEntry.getResourceId());
        resourceHistory.setResourceType(resourceEntry.getResourceType());
        resourceHistory.setVersion(resourceEntry.getVersion());
        resourceHistory.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistory.setServiceId(resourceEntry.getServiceId());
        resourceHistory.setSystemId(resourceEntry.getSystemId());
        resourceHistory.setIsDeleted(false);
        resourceHistory.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceHistory.setResourceData(resourceEntry.getResourceData());
        Mapper<ResourceHistory> mapperResourceHistory = getMappingManager().mapper(ResourceHistory.class);
        batch.add(mapperResourceHistory.saveQuery(resourceHistory));

        ResourceHistoryByService resourceHistoryByService = new ResourceHistoryByService();
        resourceHistoryByService.setResourceId(resourceEntry.getResourceId());
        resourceHistoryByService.setResourceType(resourceEntry.getResourceType());
        resourceHistoryByService.setVersion(resourceEntry.getVersion());
        resourceHistoryByService.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistoryByService.setServiceId(resourceEntry.getServiceId());
        resourceHistoryByService.setSystemId(resourceEntry.getSystemId());
        resourceHistoryByService.setIsDeleted(false);
        resourceHistoryByService.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceHistoryByService.setResourceData(resourceEntry.getResourceData());
        Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        batch.add(mapperResourceHistoryByService.saveQuery(resourceHistoryByService));

        ResourceByService resourceByService = new ResourceByService();
        resourceByService.setServiceId(resourceEntry.getServiceId());
        resourceByService.setSystemId(resourceEntry.getSystemId());
        resourceByService.setResourceType(resourceEntry.getResourceType());
        resourceByService.setResourceId(resourceEntry.getResourceId());
        resourceByService.setCurrentVersion(resourceEntry.getVersion());
        resourceByService.setUpdatedAt(resourceEntry.getCreatedAt());
        resourceByService.setPatientId(resourceEntry.getPatientId());
        resourceByService.setSchemaVersion(resourceEntry.getSchemaVersion());
        resourceByService.setResourceMetadata(resourceEntry.getResourceMetadata());
        resourceByService.setResourceData(resourceEntry.getResourceData());
        Mapper<ResourceByService> mapperResourceByService = getMappingManager().mapper(ResourceByService.class);
        batch.add(mapperResourceByService.saveQuery(resourceByService));

        if (exchangeId != null && batchId != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(batchId);
            resourceByExchangeBatch.setExchangeId(exchangeId);
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(false);
            resourceByExchangeBatch.setSchemaVersion(resourceEntry.getSchemaVersion());
            resourceByExchangeBatch.setResourceData(resourceEntry.getResourceData());
            Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = getMappingManager().mapper(ResourceByExchangeBatch.class);
            batch.add(mapperResourceByExchangeBatch.saveQuery(resourceByExchangeBatch));
        }

        getSession().execute(batch);
    }*/

    public void delete(ResourceEntry resourceEntry){
        delete(resourceEntry, null, null);
    }

    public void delete(ResourceEntry resourceEntry, UUID exchangeId, UUID batchId){
        if (resourceEntry == null) throw new IllegalArgumentException("resourceEntry is null");

        ResourceHistory resourceHistory = new ResourceHistory();
        resourceHistory.setResourceId(resourceEntry.getResourceId());
        resourceHistory.setResourceType(resourceEntry.getResourceType());
        resourceHistory.setVersion(resourceEntry.getVersion());
        resourceHistory.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistory.setServiceId(resourceEntry.getServiceId());
        resourceHistory.setSystemId(resourceEntry.getSystemId());
        resourceHistory.setIsDeleted(true);
        Mapper<ResourceHistory> mapperResourceHistory = getMappingManager().mapper(ResourceHistory.class);
        mapperResourceHistory.save(resourceHistory);

        /*ResourceHistoryByService resourceHistoryByService = new ResourceHistoryByService();
        resourceHistoryByService.setResourceId(resourceEntry.getResourceId());
        resourceHistoryByService.setResourceType(resourceEntry.getResourceType());
        resourceHistoryByService.setVersion(resourceEntry.getVersion());
        resourceHistoryByService.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistoryByService.setServiceId(resourceEntry.getServiceId());
        resourceHistoryByService.setSystemId(resourceEntry.getSystemId());
        resourceHistoryByService.setIsDeleted(true);
        Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        mapperResourceHistoryByService.save(resourceHistoryByService);

        ResourceByService resourceByService = new ResourceByService();
        resourceByService.setServiceId(resourceEntry.getServiceId());
        resourceByService.setSystemId(resourceEntry.getSystemId());
        resourceByService.setResourceType(resourceEntry.getResourceType());
        resourceByService.setResourceId(resourceEntry.getResourceId());
        Mapper<ResourceByService> mapperResourceMetadata = getMappingManager().mapper(ResourceByService.class);
        mapperResourceMetadata.save(resourceByService);*/

        //test manual insert rather than materialised view
        /*if (resourceEntry.getPatientId() != null) {
            ResourceByPatient resourceByPatient = new ResourceByPatient();
            resourceByPatient.setServiceId(resourceEntry.getServiceId());
            resourceByPatient.setSystemId(resourceEntry.getSystemId());
            resourceByPatient.setPatientId(resourceEntry.getPatientId());
            resourceByPatient.setResourceType(resourceEntry.getResourceType());
            resourceByPatient.setResourceId(resourceEntry.getResourceId());
            Mapper<ResourceByPatient> mapperResourceByPatient = getMappingManager().mapper(ResourceByPatient.class);
            mapperResourceByPatient.save(resourceByPatient);
        }*/

        if (exchangeId != null && batchId != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(batchId);
            resourceByExchangeBatch.setExchangeId(exchangeId);
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(true);
            Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = getMappingManager().mapper(ResourceByExchangeBatch.class);
            mapperResourceByExchangeBatch.save(resourceByExchangeBatch);
        }
    }
    /*public void delete(ResourceEntry resourceEntry, UUID exchangeId, UUID batchId){
        if (resourceEntry == null) throw new IllegalArgumentException("resourceEntry is null");

        BatchStatement batch = new BatchStatement();

        ResourceHistory resourceHistory = new ResourceHistory();
        resourceHistory.setResourceId(resourceEntry.getResourceId());
        resourceHistory.setResourceType(resourceEntry.getResourceType());
        resourceHistory.setVersion(resourceEntry.getVersion());
        resourceHistory.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistory.setServiceId(resourceEntry.getServiceId());
        resourceHistory.setSystemId(resourceEntry.getSystemId());
        resourceHistory.setIsDeleted(true);
        Mapper<ResourceHistory> mapperResourceHistory = getMappingManager().mapper(ResourceHistory.class);
        batch.add(mapperResourceHistory.saveQuery(resourceHistory));

        ResourceHistoryByService resourceHistoryByService = new ResourceHistoryByService();
        resourceHistoryByService.setResourceId(resourceEntry.getResourceId());
        resourceHistoryByService.setResourceType(resourceEntry.getResourceType());
        resourceHistoryByService.setVersion(resourceEntry.getVersion());
        resourceHistoryByService.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistoryByService.setServiceId(resourceEntry.getServiceId());
        resourceHistoryByService.setSystemId(resourceEntry.getSystemId());
        resourceHistoryByService.setIsDeleted(true);
        Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        batch.add(mapperResourceHistoryByService.saveQuery(resourceHistoryByService));

        ResourceByService resourceByService = new ResourceByService();
        resourceByService.setServiceId(resourceEntry.getServiceId());
        resourceByService.setSystemId(resourceEntry.getSystemId());
        resourceByService.setResourceType(resourceEntry.getResourceType());
        resourceByService.setResourceId(resourceEntry.getResourceId());
        Mapper<ResourceByService> mapperResourceMetadata = getMappingManager().mapper(ResourceByService.class);
        batch.add(mapperResourceMetadata.deleteQuery(resourceByService));

        if (exchangeId != null && batchId != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(batchId);
            resourceByExchangeBatch.setExchangeId(exchangeId);
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(true);
            Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = getMappingManager().mapper(ResourceByExchangeBatch.class);
            batch.add(mapperResourceByExchangeBatch.saveQuery(resourceByExchangeBatch));
        }

        getSession().execute(batch);
    }*/


    public ResourceHistory getByKey(String resourceType, UUID resourceId, UUID version) {
        Mapper<ResourceHistory> mapperResourceStore = getMappingManager().mapper(ResourceHistory.class);
        return mapperResourceStore.get(resourceType, resourceId, version);
    }

    /**
     * convenience fn to save repetitive code
     */
    public Resource getCurrentVersionAsResource(ResourceType resourceType, String resourceIdStr) throws Exception {
        ResourceHistory resourceHistory = getCurrentVersion(resourceType.toString(), UUID.fromString(resourceIdStr));

        if (resourceHistory == null) {
            throw new ResourceNotFoundException(resourceType, UUID.fromString(resourceIdStr));
        }

        if (resourceHistory.getIsDeleted()) {
            return null;
        } else {
            return new JsonParser().parse(resourceHistory.getResourceData());
        }
    }

    public ResourceHistory getCurrentVersion(String resourceType, UUID resourceId) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        return accessor.getCurrentVersion(resourceType, resourceId);
    }

    public List<ResourceHistory> getResourceHistory(String resourceType, UUID resourceId) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        return Lists.newArrayList(accessor.getResourceHistory(resourceType, resourceId));
    }

    public List<ResourceByPatient> getResourcesByPatient(UUID serviceId, UUID systemId, UUID patientId) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByPatient(serviceId, systemId, patientId));
    }

    public List<ResourceByPatient> getResourcesByPatient(UUID serviceId, UUID systemId, UUID patientId, String resourceType) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByPatient(serviceId, systemId, patientId, resourceType));
    }

    public List<ResourceByService> getResourcesByService(UUID serviceId, UUID systemId, String resourceType, List<UUID> resourceIds) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByService(serviceId, systemId, resourceType, resourceIds));
    }

    public List<ResourceByExchangeBatch> getResourcesForBatch(UUID batchId) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesForBatch(batchId));
    }

    public long getResourceCountByService(UUID serviceId, UUID systemId, String resourceType) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        ResultSet result = accessor.getResourceCountByService(serviceId, systemId, resourceType);
        Row row = result.one();
        return row.getLong(0);
    }

    public <T extends ResourceMetadata> ResourceMetadataIterator<T> getMetadataByService(UUID serviceId, UUID systemId, String resourceType, Class<T> classOfT) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        ResultSet result = accessor.getMetadataByService(serviceId, systemId, resourceType);
        return new ResourceMetadataIterator<>(result.iterator(), classOfT);
    }

    public long getResourceChecksum(String resourceType, UUID resourceId) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        ResultSet resultSet = accessor.getCurrentChecksum(resourceType, resourceId);
        Row row = resultSet.one();
        if (row != null) {
            return row.getLong(0);
        } else {
            return Long.MIN_VALUE;
        }
    }

}
