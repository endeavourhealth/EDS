package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.ehr.accessors.ResourceAccessor;
import org.endeavourhealth.core.data.ehr.accessors.ResourceHistoryAccessor;
import org.endeavourhealth.core.data.ehr.models.*;
import org.endeavourhealth.core.fhirStorage.metadata.ResourceMetadata;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ResourceRepository extends Repository {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRepository.class);
    private static final ParserPool PARSER_POOL = new ParserPool();

    public void save(ResourceEntry resourceEntry){
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
        save(resourceHistory);

        //remove this table
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
        mapperResourceHistoryByService.save(resourceHistoryByService);*/

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
        save(resourceByService);

        if (resourceEntry.getExchangeId() != null && resourceEntry.getBatchId() != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(resourceEntry.getBatchId());
            resourceByExchangeBatch.setExchangeId(resourceEntry.getExchangeId());
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(false);
            resourceByExchangeBatch.setSchemaVersion(resourceEntry.getSchemaVersion());
            resourceByExchangeBatch.setResourceData(resourceEntry.getResourceData());
            save(resourceByExchangeBatch);
        }
    }


    public void delete(ResourceEntry resourceEntry){
        if (resourceEntry == null) throw new IllegalArgumentException("resourceEntry is null");

        ResourceHistory resourceHistory = new ResourceHistory();
        resourceHistory.setResourceId(resourceEntry.getResourceId());
        resourceHistory.setResourceType(resourceEntry.getResourceType());
        resourceHistory.setVersion(resourceEntry.getVersion());
        resourceHistory.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistory.setServiceId(resourceEntry.getServiceId());
        resourceHistory.setSystemId(resourceEntry.getSystemId());
        resourceHistory.setIsDeleted(true);
        save(resourceHistory);

        //remove this table
        /*ResourceHistoryByService resourceHistoryByService = new ResourceHistoryByService();
        resourceHistoryByService.setResourceId(resourceEntry.getResourceId());
        resourceHistoryByService.setResourceType(resourceEntry.getResourceType());
        resourceHistoryByService.setVersion(resourceEntry.getVersion());
        resourceHistoryByService.setCreatedAt(resourceEntry.getCreatedAt());
        resourceHistoryByService.setServiceId(resourceEntry.getServiceId());
        resourceHistoryByService.setSystemId(resourceEntry.getSystemId());
        resourceHistoryByService.setIsDeleted(true);
        Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        mapperResourceHistoryByService.save(resourceHistoryByService);*/

        ResourceByService resourceByService = new ResourceByService();
        resourceByService.setServiceId(resourceEntry.getServiceId());
        resourceByService.setSystemId(resourceEntry.getSystemId());
        resourceByService.setResourceType(resourceEntry.getResourceType());
        resourceByService.setResourceId(resourceEntry.getResourceId());
        resourceByService.setCurrentVersion(resourceEntry.getVersion()); //was missing - so it wasn't clear when something was deleted
        resourceByService.setUpdatedAt(resourceEntry.getCreatedAt()); //was missing - so it wasn't clear when something was deleted
        save(resourceByService);

        if (resourceEntry.getExchangeId() != null && resourceEntry.getBatchId() != null) {
            ResourceByExchangeBatch resourceByExchangeBatch = new ResourceByExchangeBatch();
            resourceByExchangeBatch.setBatchId(resourceEntry.getBatchId());
            resourceByExchangeBatch.setExchangeId(resourceEntry.getExchangeId());
            resourceByExchangeBatch.setResourceType(resourceEntry.getResourceType());
            resourceByExchangeBatch.setResourceId(resourceEntry.getResourceId());
            resourceByExchangeBatch.setVersion(resourceEntry.getVersion());
            resourceByExchangeBatch.setIsDeleted(true);
            save(resourceByExchangeBatch);
        }
    }

    public void save(ResourceHistory resourceHistory) {
        Mapper<ResourceHistory> mapper = getMappingManager().mapper(ResourceHistory.class);
        mapper.save(resourceHistory);
    }

    public void save(ResourceByService resourceByService) {
        Mapper<ResourceByService> mapper = getMappingManager().mapper(ResourceByService.class);
        mapper.save(resourceByService);
    }

    public void save(ResourceByExchangeBatch resourceByExchangeBatch) {
        Mapper<ResourceByExchangeBatch> mapper = getMappingManager().mapper(ResourceByExchangeBatch.class);
        mapper.save(resourceByExchangeBatch);
    }

    public ResourceHistory getResourceHistoryByKey(UUID resourceId, String resourceType, UUID version) {
        Mapper<ResourceHistory> mapper = getMappingManager().mapper(ResourceHistory.class);
        return mapper.get(resourceId, resourceType, version);
    }

    public ResourceByService getResourceByServiceByKey(UUID serviceId, UUID systemId, String resourceType, UUID resourceId) {
        Mapper<ResourceByService> mapper = getMappingManager().mapper(ResourceByService.class);
        return mapper.get(serviceId, systemId, resourceType, resourceId);
    }

    public ResourceByExchangeBatch getResourceByExchangeBatchByKey(UUID batchId, String resourceType, UUID resourceId, UUID version) {
        Mapper<ResourceByExchangeBatch> mapper = getMappingManager().mapper(ResourceByExchangeBatch.class);
        return mapper.get(batchId, resourceType, resourceId, version);
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
            return PARSER_POOL.parse(resourceHistory.getResourceData());
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

    public List<ResourceByExchangeBatch> getResourcesForBatch(UUID batchId, String resourceType) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesForBatch(batchId, resourceType));
    }

    public List<ResourceByExchangeBatch> getResourcesForBatch(UUID batchId, String resourceType, UUID resourceId) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesForBatch(batchId, resourceType, resourceId));
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

    public Long getResourceChecksum(String resourceType, UUID resourceId) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        ResultSet resultSet = accessor.getCurrentChecksum(resourceType, resourceId);
        Row row = resultSet.one();
        if (row != null) {
            return Long.valueOf(row.getLong(0));
        } else {
            return null;
        }
    }

    public void hardDelete(ResourceEntry keys) {

        Mapper<ResourceHistory> mapperResourceHistory = getMappingManager().mapper(ResourceHistory.class);
        mapperResourceHistory.delete(keys.getResourceId(), keys.getResourceType(), keys.getVersion());

        //remove this table
        /*Mapper<ResourceHistoryByService> mapperResourceHistoryByService = getMappingManager().mapper(ResourceHistoryByService.class);
        mapperResourceHistoryByService.delete(keys.getServiceId(), keys.getSystemId(), keys.getResourceType(), keys.getResourceId(), keys.getVersion());*/

        Mapper<ResourceByService> mapperResourceByService = getMappingManager().mapper(ResourceByService.class);
        mapperResourceByService.delete(keys.getServiceId(), keys.getSystemId(), keys.getResourceType(), keys.getResourceId());

        Mapper<ResourceByExchangeBatch> mapperResourceByExchangeBatch = getMappingManager().mapper(ResourceByExchangeBatch.class);
        mapperResourceByExchangeBatch.delete(keys.getBatchId(), keys.getResourceType(), keys.getResourceId(), keys.getVersion());
    }

    /**
     * tests if we have any patient data stored for the given service and system
     */
    public boolean dataExists(UUID serviceId, UUID systemId) {

        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        Result<ResourceByService> result = accessor.getFirstResourceByService(serviceId, systemId, ResourceType.Patient.toString());
        return result.iterator().hasNext();
    }

    public ResourceByService getFirstResourceByService(UUID serviceId, UUID systemId, ResourceType resourceType) {

        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        Result<ResourceByService> result = accessor.getFirstResourceByService(serviceId, systemId, resourceType.toString());
        Iterator<ResourceByService> it = result.iterator();
        if (it.hasNext()) {
            return it.next();
        } else {
            return null;
        }
    }

    public List<ResourceByService> getResourcesByService(UUID serviceId, UUID systemId, String resourceType) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByService(serviceId, systemId, resourceType));
    }

    //TODO - to be removed
    public ResourceByExchangeBatch getFirstResourceByExchangeBatch(String resourceType, UUID resourceId) {
        ResourceAccessor accessor = getMappingManager().createAccessor(ResourceAccessor.class);
        return accessor.getFirstResourceByExchangeBatch(resourceType, resourceId);
    }
}
