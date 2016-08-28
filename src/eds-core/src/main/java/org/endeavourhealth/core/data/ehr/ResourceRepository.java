package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.ResourceHistoryAccessor;
import org.endeavourhealth.core.data.ehr.models.*;
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
    }

    public void delete(ResourceEntry resourceEntry){
        delete(resourceEntry, null, null);
    }

    public void delete(ResourceEntry resourceEntry, UUID exchangeId, UUID batchId){
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
    }

    public void save(ResourceTypesUsed resourceTypesUsed) {
        if (resourceTypesUsed == null) {
            throw new IllegalArgumentException("resourceTypesUsed is null");
        }

        Mapper<ResourceTypesUsed> mapper = getMappingManager().mapper(ResourceTypesUsed.class);
        mapper.save(resourceTypesUsed);
    }

    public ResourceHistory getByKey(String resourceType, UUID resourceId, UUID version) {
        Mapper<ResourceHistory> mapperResourceStore = getMappingManager().mapper(ResourceHistory.class);
        return mapperResourceStore.get(resourceType, resourceId, version);
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
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByPatient(serviceId, systemId, patientId));
    }

    public List<ResourceByPatient> getResourcesByPatient(UUID serviceId, UUID systemId, UUID patientId, String resourceType) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        return Lists.newArrayList(accessor.getResourcesByPatient(serviceId, systemId, patientId, resourceType));
    }

    public List<ResourceTypesUsed> getResourcesTypesUsed(UUID serviceId, UUID systemId) {
        ResourceHistoryAccessor accessor = getMappingManager().createAccessor(ResourceHistoryAccessor.class);
        return Lists.newArrayList(accessor.getResourceTypesUsed(serviceId, systemId));
    }

}
