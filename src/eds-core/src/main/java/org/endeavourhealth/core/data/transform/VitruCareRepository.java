package org.endeavourhealth.core.data.transform;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.transform.accessors.VitruCareAccessor;
import org.endeavourhealth.core.data.transform.models.VitruCarePatientIdMap;

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class VitruCareRepository extends Repository {

    public void saveVitruCareIdMapping(UUID edsPatientId, UUID serviceId, UUID systemId, String virtruCareId) {
        VitruCarePatientIdMap o = new VitruCarePatientIdMap();
        o.setEdsPatientId(edsPatientId);
        o.setServiceId(serviceId);
        o.setSystemId(systemId);
        o.setVitruCareId(virtruCareId);
        o.setCreatedAt(new Date());
        saveVitruCareIdMapping(o);
    }


    public void saveVitruCareIdMapping(VitruCarePatientIdMap mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("mapping is null");
        }

        Mapper<VitruCarePatientIdMap> mapper = getMappingManager().mapper(VitruCarePatientIdMap.class);
        mapper.save(mapping);
    }

    public String getVitruCareId(UUID edsPatientId) {

        VitruCareAccessor accessor = getMappingManager().createAccessor(VitruCareAccessor.class);
        Iterator<VitruCarePatientIdMap> iterator = accessor.getVitruCareIdMapping(edsPatientId).iterator();
        if (iterator.hasNext()) {
            VitruCarePatientIdMap mapping = iterator.next();
            return mapping.getVitruCareId();
        } else {
            return null;
        }
    }
}
