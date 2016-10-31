package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperEpisodeOfCare extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        EpisodeOfCare episodeOfCare = (EpisodeOfCare)resource;

        if (episodeOfCare.hasIdentifier()) {
            super.mapIdentifiers(episodeOfCare.getIdentifier(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasCondition()) {
            super.mapReferences(episodeOfCare.getCondition(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasPatient()) {
            super.mapReference(episodeOfCare.getPatient(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasManagingOrganization()) {
            super.mapReference(episodeOfCare.getManagingOrganization(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasReferralRequest()) {
            super.mapReferences(episodeOfCare.getReferralRequest(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasCareManager()) {
            super.mapReference(episodeOfCare.getCareManager(), resource, serviceId, systemId);
        }
        if (episodeOfCare.hasCareTeam()) {
            for (EpisodeOfCare.EpisodeOfCareCareTeamComponent careTeam: episodeOfCare.getCareTeam()) {
                if (careTeam.hasMember()) {
                    super.mapReference(careTeam.getMember(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(episodeOfCare, serviceId, systemId, mapResourceId);
    }
}
