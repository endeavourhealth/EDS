package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Schedule;

import java.util.UUID;

public class IdMapperSchedule extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Schedule schedule = (Schedule)resource;

        super.mapResourceId(schedule, serviceId, systemInstanceId);
        super.mapExtensions(schedule, serviceId, systemInstanceId);

        if (schedule.hasIdentifier()) {
            super.mapIdentifiers(schedule.getIdentifier(), serviceId, systemInstanceId);
        }
        if (schedule.hasActor()) {
            super.mapReference(schedule.getActor(), serviceId, systemInstanceId);
        }
    }
}
