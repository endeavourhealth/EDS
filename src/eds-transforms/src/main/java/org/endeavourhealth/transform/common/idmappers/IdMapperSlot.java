package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Slot;

import java.util.UUID;

public class IdMapperSlot extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Slot slot = (Slot)resource;

        if (slot.hasIdentifier()) {
            super.mapIdentifiers(slot.getIdentifier(), resource, serviceId, systemId);
        }
        if (slot.hasSchedule()) {
            super.mapReference(slot.getSchedule(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(slot, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {
        throw new PatientResourceException(resource, true);
    }
}
