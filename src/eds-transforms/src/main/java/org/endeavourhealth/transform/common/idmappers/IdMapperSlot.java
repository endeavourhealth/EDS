package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Slot;

import java.util.UUID;

public class IdMapperSlot extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Slot slot = (Slot)resource;

        super.mapCommonResourceFields(slot, serviceId, systemId, mapResourceId);

        if (slot.hasIdentifier()) {
            super.mapIdentifiers(slot.getIdentifier(), resource, serviceId, systemId);
        }
        if (slot.hasSchedule()) {
            super.mapReference(slot.getSchedule(), resource, serviceId, systemId);
        }
    }
}
