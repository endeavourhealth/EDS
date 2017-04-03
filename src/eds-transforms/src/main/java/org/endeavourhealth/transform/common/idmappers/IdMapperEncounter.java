package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperEncounter extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Encounter encounter = (Encounter)resource;

        if (encounter.hasIdentifier()) {
            super.mapIdentifiers(encounter.getIdentifier(), resource, serviceId, systemId);
        }
        if (encounter.hasPatient()) {
            super.mapReference(encounter.getPatient(), resource, serviceId, systemId);
        }
        if (encounter.hasEpisodeOfCare()) {
            super.mapReferences(encounter.getEpisodeOfCare(), resource, serviceId, systemId);
        }
        if (encounter.hasIncomingReferral()) {
            super.mapReferences(encounter.getIncomingReferral(), resource, serviceId, systemId);
        }
        if (encounter.hasParticipant()) {
            for (Encounter.EncounterParticipantComponent participant: encounter.getParticipant()) {
                if (participant.hasIndividual()) {
                    super.mapReference(participant.getIndividual(), resource, serviceId, systemId);
                }
            }
        }
        if (encounter.hasAppointment()) {
            super.mapReference(encounter.getAppointment(), resource, serviceId, systemId);
        }
        if (encounter.hasIndication()) {
            super.mapReferences(encounter.getIndication(), resource, serviceId, systemId);
        }
        if (encounter.hasLocation()) {
            for (Encounter.EncounterLocationComponent location: encounter.getLocation()) {
                if (location.hasLocation()) {
                    super.mapReference(location.getLocation(), resource, serviceId, systemId);
                }
            }
        }
        if (encounter.hasServiceProvider()) {
            super.mapReference(encounter.getServiceProvider(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(encounter, serviceId, systemId, mapResourceId);
    }


    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Encounter encounter = (Encounter)resource;
        if (encounter.hasPatient()) {
            return ReferenceHelper.getReferenceId(encounter.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
