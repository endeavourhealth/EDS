package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Slot;

import java.util.UUID;

public class IdMapperSlot extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Slot slot = (Slot)resource;

        super.mapResourceId(slot, serviceId, systemId);
        super.mapExtensions(slot, serviceId, systemId);

        if (slot.hasIdentifier()) {
            super.mapIdentifiers(slot.getIdentifier(), resource, serviceId, systemId);
        }
        if (slot.hasSchedule()) {
            super.mapReference(slot.getSchedule(), resource, serviceId, systemId);
        }
    }
}
