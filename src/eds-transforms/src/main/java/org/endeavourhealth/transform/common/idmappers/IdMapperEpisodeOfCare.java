package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperEpisodeOfCare extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        EpisodeOfCare episodeOfCare = (EpisodeOfCare)resource;

        super.mapResourceId(episodeOfCare, serviceId, systemId);
        super.mapExtensions(episodeOfCare, serviceId, systemId);

        if (episodeOfCare.hasIdentifier()) {
            super.mapIdentifiers(episodeOfCare.getIdentifier(), serviceId, systemId);
        }
        if (episodeOfCare.hasCondition()) {
            super.mapReferences(episodeOfCare.getCondition(), serviceId, systemId);
        }
        if (episodeOfCare.hasPatient()) {
            super.mapReference(episodeOfCare.getPatient(), serviceId, systemId);
        }
        if (episodeOfCare.hasManagingOrganization()) {
            super.mapReference(episodeOfCare.getManagingOrganization(), serviceId, systemId);
        }
        if (episodeOfCare.hasReferralRequest()) {
            super.mapReferences(episodeOfCare.getReferralRequest(), serviceId, systemId);
        }
        if (episodeOfCare.hasCareManager()) {
            super.mapReference(episodeOfCare.getCareManager(), serviceId, systemId);
        }
        if (episodeOfCare.hasCareTeam()) {
            for (EpisodeOfCare.EpisodeOfCareCareTeamComponent careTeam: episodeOfCare.getCareTeam()) {
                if (careTeam.hasMember()) {
                    super.mapReference(careTeam.getMember(), serviceId, systemId);
                }
            }
        }
    }
}
