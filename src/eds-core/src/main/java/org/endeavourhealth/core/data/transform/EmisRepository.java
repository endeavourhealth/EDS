package org.endeavourhealth.core.data.transform;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.transform.accessors.EmisAccessor;
import org.endeavourhealth.core.data.transform.models.EmisAdminResourceCache;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;

import java.util.Iterator;
import java.util.List;

public class EmisRepository extends Repository {

    public void save(EmisCsvCodeMap mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("mapping is null");
        }

        Mapper<EmisCsvCodeMap> mapper = getMappingManager().mapper(EmisCsvCodeMap.class);
        mapper.save(mapping);
    }

    public EmisCsvCodeMap getMostRecentCode(String dataSharingAgreementGuid, boolean medication, Long codeId) {

        EmisAccessor accessor = getMappingManager().createAccessor(EmisAccessor.class);
        Iterator<EmisCsvCodeMap> iterator = accessor.getMostRecentCode(dataSharingAgreementGuid, medication, codeId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public void save(EmisAdminResourceCache resourceCache) {
        if (resourceCache == null) {
            throw new IllegalArgumentException("resourceCache is null");
        }

        Mapper<EmisAdminResourceCache> mapper = getMappingManager().mapper(EmisAdminResourceCache.class);
        mapper.save(resourceCache);
    }

    public void delete(EmisAdminResourceCache resourceCache) {
        if (resourceCache == null) {
            throw new IllegalArgumentException("resourceCache is null");
        }

        Mapper<EmisAdminResourceCache> mapper = getMappingManager().mapper(EmisAdminResourceCache.class);
        mapper.delete(resourceCache);
    }

    public List<EmisAdminResourceCache> getCachedResources(String dataSharingAgreementGuid) {

        EmisAccessor accessor = getMappingManager().createAccessor(EmisAccessor.class);
        return Lists.newArrayList(accessor.getCachedResources(dataSharingAgreementGuid));
    }
}
