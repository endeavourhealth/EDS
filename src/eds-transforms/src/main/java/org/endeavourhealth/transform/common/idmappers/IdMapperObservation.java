package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperObservation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Observation observation = (Observation)resource;

        super.mapResourceId(observation, serviceId, systemId);
        super.mapExtensions(observation, serviceId, systemId);

        if (observation.hasIdentifier()) {
            super.mapIdentifiers(observation.getIdentifier(), serviceId, systemId);
        }
        if (observation.hasSubject()) {
            super.mapReference(observation.getSubject(), serviceId, systemId);
        }
        if (observation.hasEncounter()) {
            super.mapReference(observation.getEncounter(), serviceId, systemId);
        }
        if (observation.hasPerformer()) {
            super.mapReferences(observation.getPerformer(), serviceId, systemId);
        }
        if (observation.hasSpecimen()) {
            super.mapReference(observation.getSpecimen(), serviceId, systemId);
        }
        if (observation.hasDevice()) {
            super.mapReference(observation.getDevice(), serviceId, systemId);
        }
        if (observation.hasRelated()) {
            for (Observation.ObservationRelatedComponent related: observation.getRelated()) {
                if (related.hasTarget()) {
                    super.mapReference(related.getTarget(), serviceId, systemId);
                }
            }
        }
    }
}
