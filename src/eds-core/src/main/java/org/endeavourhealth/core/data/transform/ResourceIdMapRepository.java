package org.endeavourhealth.core.data.transform;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.transform.accessors.ResourceIdMapAccessor;
import org.endeavourhealth.core.data.transform.models.ResourceIdMap;
import org.endeavourhealth.core.data.transform.models.ResourceIdMapByEdsId;

import java.util.Iterator;
import java.util.UUID;

public class ResourceIdMapRepository extends Repository {

    public void insert(ResourceIdMap resourceIdMap) {
        if (resourceIdMap == null) {
            throw new IllegalArgumentException("resourceIdMap is null");
        }

        Mapper<ResourceIdMap> mapper = getMappingManager().mapper(ResourceIdMap.class);
        mapper.save(resourceIdMap);
    }

    public ResourceIdMap getResourceIdMap(UUID serviceId, UUID systemId, String resourceType, String sourceId) {

        ResourceIdMapAccessor accessor = getMappingManager().createAccessor(ResourceIdMapAccessor.class);
        Iterator<ResourceIdMap> iterator = accessor.getResourceIdMap(serviceId, systemId, resourceType, sourceId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public ResourceIdMapByEdsId getResourceIdMapByEdsId(String resourceType, String edsId) {
        return getResourceIdMapByEdsId(resourceType, UUID.fromString(edsId));
    }

    public ResourceIdMapByEdsId getResourceIdMapByEdsId(String resourceType, UUID edsId) {

        ResourceIdMapAccessor accessor = getMappingManager().createAccessor(ResourceIdMapAccessor.class);
        Iterator<ResourceIdMapByEdsId> iterator = accessor.getResourceIdMapByEdsId(resourceType, edsId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;

        }
    }
}
