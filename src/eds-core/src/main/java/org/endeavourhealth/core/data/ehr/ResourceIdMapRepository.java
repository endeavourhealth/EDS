package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentGlobalAccessor;
import org.endeavourhealth.core.data.ehr.accessors.ResourceIdMapAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;
import org.endeavourhealth.core.data.ehr.models.ResourceIdMap;

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

    public ResourceIdMap getResourceIdMap(UUID serviceId, UUID systemInstanceId, String resourceType, String sourceId) {

        ResourceIdMapAccessor accessor = getMappingManager().createAccessor(ResourceIdMapAccessor.class);
        Iterator<ResourceIdMap> iterator = accessor.getResourceIdMap(serviceId, systemInstanceId, resourceType, sourceId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}
