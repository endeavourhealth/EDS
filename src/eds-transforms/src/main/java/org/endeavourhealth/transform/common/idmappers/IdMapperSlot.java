package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Slot;

import java.util.UUID;

public class IdMapperSlot extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Slot slot = (Slot)resource;

        super.mapResourceId(slot, serviceId, systemInstanceId);
        super.mapExtensions(slot, serviceId, systemInstanceId);

        if (slot.hasIdentifier()) {
            super.mapIdentifiers(slot.getIdentifier(), serviceId, systemInstanceId);
        }
        if (slot.hasSchedule()) {
            super.mapReference(slot.getSchedule(), serviceId, systemInstanceId);
        }
    }
}
