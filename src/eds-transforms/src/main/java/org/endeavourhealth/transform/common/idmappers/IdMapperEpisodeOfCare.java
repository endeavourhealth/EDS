package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperEpisodeOfCare extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        EpisodeOfCare episodeOfCare = (EpisodeOfCare)resource;

        super.mapResourceId(episodeOfCare, serviceId, systemInstanceId);
        super.mapExtensions(episodeOfCare, serviceId, systemInstanceId);

        if (episodeOfCare.hasIdentifier()) {
            super.mapIdentifiers(episodeOfCare.getIdentifier(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasCondition()) {
            super.mapReferences(episodeOfCare.getCondition(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasPatient()) {
            super.mapReference(episodeOfCare.getPatient(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasManagingOrganization()) {
            super.mapReference(episodeOfCare.getManagingOrganization(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasReferralRequest()) {
            super.mapReferences(episodeOfCare.getReferralRequest(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasCareManager()) {
            super.mapReference(episodeOfCare.getCareManager(), serviceId, systemInstanceId);
        }
        if (episodeOfCare.hasCareTeam()) {
            for (EpisodeOfCare.EpisodeOfCareCareTeamComponent careTeam: episodeOfCare.getCareTeam()) {
                if (careTeam.hasMember()) {
                    super.mapReference(careTeam.getMember(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
