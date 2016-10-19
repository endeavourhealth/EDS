package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperObservation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        Observation observation = (Observation)resource;

        super.mapResourceId(observation, serviceId, systemId);
        super.mapExtensions(observation, serviceId, systemId);

        if (observation.hasIdentifier()) {
            super.mapIdentifiers(observation.getIdentifier(), resource, serviceId, systemId);
        }
        if (observation.hasSubject()) {
            super.mapReference(observation.getSubject(), resource, serviceId, systemId);
        }
        if (observation.hasEncounter()) {
            super.mapReference(observation.getEncounter(), resource, serviceId, systemId);
        }
        if (observation.hasPerformer()) {
            super.mapReferences(observation.getPerformer(), resource, serviceId, systemId);
        }
        if (observation.hasSpecimen()) {
            super.mapReference(observation.getSpecimen(), resource, serviceId, systemId);
        }
        if (observation.hasDevice()) {
            super.mapReference(observation.getDevice(), resource, serviceId, systemId);
        }
        if (observation.hasRelated()) {
            for (Observation.ObservationRelatedComponent related: observation.getRelated()) {
                if (related.hasTarget()) {
                    super.mapReference(related.getTarget(), resource, serviceId, systemId);
                }
            }
        }
    }
}
