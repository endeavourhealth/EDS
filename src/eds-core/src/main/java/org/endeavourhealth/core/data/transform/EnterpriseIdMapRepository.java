package org.endeavourhealth.core.data.transform;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.transform.accessors.EnterpriseIdMapAccessor;
import org.endeavourhealth.core.data.transform.models.EnterpriseIdMap;

import java.util.Iterator;
import java.util.UUID;

public class EnterpriseIdMapRepository extends Repository {

    public void insert(EnterpriseIdMap enterpriseIdMap) {
        if (enterpriseIdMap == null) {
            throw new IllegalArgumentException("enterpriseIdMap is null");
        }

        Mapper<EnterpriseIdMap> mapper = getMappingManager().mapper(EnterpriseIdMap.class);
        mapper.save(enterpriseIdMap);
    }

    public UUID getEnterpriseIdMappingUuid(String resourceType, UUID resourceId) {
        EnterpriseIdMap mapping = getEnterpriseIdMapping(resourceType, resourceId);
        if (mapping != null) {
            return mapping.getEnterpriseId();
        } else {
            return null;
        }
    }

    public EnterpriseIdMap getEnterpriseIdMapping(String resourceType, UUID resourceId) {

        EnterpriseIdMapAccessor accessor = getMappingManager().createAccessor(EnterpriseIdMapAccessor.class);
        Iterator<EnterpriseIdMap> iterator = accessor.getEnterpriseIdMapping(resourceType, resourceId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public UUID createEnterpriseIdMappingUuid(String resourceType, UUID resourceId) {

        EnterpriseIdMap mapping = new EnterpriseIdMap();
        mapping.setResourceType(resourceType);
        mapping.setResourceId(resourceId);
        mapping.setEnterpriseId(UUID.randomUUID());
        insert(mapping);

        return mapping.getEnterpriseId();
    }

    public void setEnterpriseIdMapping(String resourceType, UUID resourceId, UUID enterpriseId) {
        EnterpriseIdMap mapping = new EnterpriseIdMap();
        mapping.setResourceType(resourceType);
        mapping.setResourceId(resourceId);
        mapping.setEnterpriseId(enterpriseId);
        insert(mapping);
    }

}
