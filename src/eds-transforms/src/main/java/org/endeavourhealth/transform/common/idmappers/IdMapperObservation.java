package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperObservation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Observation observation = (Observation)resource;

        super.mapResourceId(observation, serviceId, systemInstanceId);
        super.mapExtensions(observation, serviceId, systemInstanceId);

        if (observation.hasIdentifier()) {
            super.mapIdentifiers(observation.getIdentifier(), serviceId, systemInstanceId);
        }
        if (observation.hasSubject()) {
            super.mapReference(observation.getSubject(), serviceId, systemInstanceId);
        }
        if (observation.hasEncounter()) {
            super.mapReference(observation.getEncounter(), serviceId, systemInstanceId);
        }
        if (observation.hasPerformer()) {
            super.mapReferences(observation.getPerformer(), serviceId, systemInstanceId);
        }
        if (observation.hasSpecimen()) {
            super.mapReference(observation.getSpecimen(), serviceId, systemInstanceId);
        }
        if (observation.hasDevice()) {
            super.mapReference(observation.getDevice(), serviceId, systemInstanceId);
        }
        if (observation.hasRelated()) {
            for (Observation.ObservationRelatedComponent related: observation.getRelated()) {
                if (related.hasTarget()) {
                    super.mapReference(related.getTarget(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
