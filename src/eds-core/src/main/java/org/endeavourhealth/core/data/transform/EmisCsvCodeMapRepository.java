package org.endeavourhealth.core.data.transform;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.transform.accessors.EmisCsvCodeMapAccessor;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;

import java.util.Iterator;
import java.util.UUID;

public class EmisCsvCodeMapRepository extends Repository {

    public void save(EmisCsvCodeMap mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("mapping is null");
        }

        Mapper<EmisCsvCodeMap> mapper = getMappingManager().mapper(EmisCsvCodeMap.class);
        mapper.save(mapping);
    }

    public EmisCsvCodeMap getMostRecent(UUID serviceId, UUID systemInstanceId, boolean medication, Long codeId) {

        EmisCsvCodeMapAccessor accessor = getMappingManager().createAccessor(EmisCsvCodeMapAccessor.class);
        Iterator<EmisCsvCodeMap> iterator = accessor.getMostRecent(serviceId, systemInstanceId, medication, codeId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }


}
