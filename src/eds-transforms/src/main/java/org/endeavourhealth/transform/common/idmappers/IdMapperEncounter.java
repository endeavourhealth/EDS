package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperEncounter extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Encounter encounter = (Encounter)resource;

        super.mapResourceId(encounter, serviceId, systemInstanceId);
        super.mapExtensions(encounter, serviceId, systemInstanceId);

        if (encounter.hasIdentifier()) {
            super.mapIdentifiers(encounter.getIdentifier(), serviceId, systemInstanceId);
        }
        if (encounter.hasPatient()) {
            super.mapReference(encounter.getPatient(), serviceId, systemInstanceId);
        }
        if (encounter.hasEpisodeOfCare()) {
            super.mapReferences(encounter.getEpisodeOfCare(), serviceId, systemInstanceId);
        }
        if (encounter.hasIncomingReferral()) {
            super.mapReferences(encounter.getIncomingReferral(), serviceId, systemInstanceId);
        }
        if (encounter.hasParticipant()) {
            for (Encounter.EncounterParticipantComponent participant: encounter.getParticipant()) {
                if (participant.hasIndividual()) {
                    super.mapReference(participant.getIndividual(), serviceId, systemInstanceId);
                }
            }
        }
        if (encounter.hasAppointment()) {
            super.mapReference(encounter.getAppointment(), serviceId, systemInstanceId);
        }
        if (encounter.hasIndication()) {
            super.mapReferences(encounter.getIndication(), serviceId, systemInstanceId);
        }
        if (encounter.hasLocation()) {
            for (Encounter.EncounterLocationComponent location: encounter.getLocation()) {
                if (location.hasLocation()) {
                    super.mapReference(location.getLocation(), serviceId, systemInstanceId);
                }
            }
        }
        if (encounter.hasServiceProvider()) {
            super.mapReference(encounter.getServiceProvider(), serviceId, systemInstanceId);
        }
    }
}
