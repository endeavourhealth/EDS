package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperEncounter extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Encounter encounter = (Encounter)resource;

        super.mapResourceId(encounter, serviceId, systemId);
        super.mapExtensions(encounter, serviceId, systemId);

        if (encounter.hasIdentifier()) {
            super.mapIdentifiers(encounter.getIdentifier(), serviceId, systemId);
        }
        if (encounter.hasPatient()) {
            super.mapReference(encounter.getPatient(), serviceId, systemId);
        }
        if (encounter.hasEpisodeOfCare()) {
            super.mapReferences(encounter.getEpisodeOfCare(), serviceId, systemId);
        }
        if (encounter.hasIncomingReferral()) {
            super.mapReferences(encounter.getIncomingReferral(), serviceId, systemId);
        }
        if (encounter.hasParticipant()) {
            for (Encounter.EncounterParticipantComponent participant: encounter.getParticipant()) {
                if (participant.hasIndividual()) {
                    super.mapReference(participant.getIndividual(), serviceId, systemId);
                }
            }
        }
        if (encounter.hasAppointment()) {
            super.mapReference(encounter.getAppointment(), serviceId, systemId);
        }
        if (encounter.hasIndication()) {
            super.mapReferences(encounter.getIndication(), serviceId, systemId);
        }
        if (encounter.hasLocation()) {
            for (Encounter.EncounterLocationComponent location: encounter.getLocation()) {
                if (location.hasLocation()) {
                    super.mapReference(location.getLocation(), serviceId, systemId);
                }
            }
        }
        if (encounter.hasServiceProvider()) {
            super.mapReference(encounter.getServiceProvider(), serviceId, systemId);
        }
    }
}
