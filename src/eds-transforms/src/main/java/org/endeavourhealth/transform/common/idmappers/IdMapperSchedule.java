package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Schedule;

import java.util.UUID;

public class IdMapperSchedule extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Schedule schedule = (Schedule)resource;

        super.mapCommonResourceFields(schedule, serviceId, systemId, mapResourceId);

        if (schedule.hasIdentifier()) {
            super.mapIdentifiers(schedule.getIdentifier(), resource, serviceId, systemId);
        }
        if (schedule.hasActor()) {
            super.mapReference(schedule.getActor(), resource, serviceId, systemId);
        }
    }
}
