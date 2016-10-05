package org.endeavourhealth.core.data.transform;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.transform.accessors.EnterpriseIdMapAccessor;
import org.endeavourhealth.core.data.transform.models.EnterpriseIdMap;
import org.endeavourhealth.core.data.transform.models.EnterpriseOrganisationIdMap;

import java.util.Iterator;
import java.util.UUID;

public class EnterpriseIdMapRepository extends Repository {

    public Integer getEnterpriseIdMappingId(String resourceType, UUID resourceId) {
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

    public void saveEnterpriseIdMapping(String resourceType, UUID resourceId, Integer enterpriseId) {
        EnterpriseIdMap mapping = new EnterpriseIdMap();
        mapping.setResourceType(resourceType);
        mapping.setResourceId(resourceId);
        mapping.setEnterpriseId(enterpriseId);

        Mapper<EnterpriseIdMap> mapper = getMappingManager().mapper(EnterpriseIdMap.class);
        mapper.save(mapping);
    }

    public int getMaxEnterpriseId(String resourceType) {
        EnterpriseIdMapAccessor accessor = getMappingManager().createAccessor(EnterpriseIdMapAccessor.class);
        ResultSet resultSet = accessor.getMaxEnterpriseId(resourceType);
        Row row = resultSet.one();
        if (row.isNull(0)) {
            return 0;
        } else {
            return row.getInt(0);
        }
    }

    public Integer getEnterpriseOrganisationIdMapping(String odsCode) {

        EnterpriseIdMapAccessor accessor = getMappingManager().createAccessor(EnterpriseIdMapAccessor.class);
        Iterator<EnterpriseOrganisationIdMap> iterator = accessor.getEnterpriseIdMapping(odsCode).iterator();
        if (iterator.hasNext()) {
            EnterpriseOrganisationIdMap mapping = iterator.next();
            return mapping.getEnterpriseId();
        } else {
            return null;
        }
    }

    public void saveEnterpriseOrganisationIdMapping(String odsCode, Integer enterpriseId) {
        EnterpriseOrganisationIdMap mapping = new EnterpriseOrganisationIdMap();
        mapping.setOrganisationOdsCode(odsCode);
        mapping.setEnterpriseId(enterpriseId);

        Mapper<EnterpriseOrganisationIdMap> mapper = getMappingManager().mapper(EnterpriseOrganisationIdMap.class);
        mapper.save(mapping);
    }
}
