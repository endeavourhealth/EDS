package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperObservation extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Observation observation = (Observation)resource;

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

        return super.mapCommonResourceFields(observation, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Observation observation = (Observation)resource;
        if (observation.hasSubject()) {
            return ReferenceHelper.getReferenceId(observation.getSubject(), ResourceType.Patient);
        }
        return null;
    }
}
