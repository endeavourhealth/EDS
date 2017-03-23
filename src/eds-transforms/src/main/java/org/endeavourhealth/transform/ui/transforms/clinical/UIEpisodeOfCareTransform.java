package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIEpisodeOfCare;
import org.endeavourhealth.transform.ui.models.types.UIPeriod;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class UIEpisodeOfCareTransform extends UIClinicalTransform<EpisodeOfCare, UIEpisodeOfCare> {

    public List<UIEpisodeOfCare> transform(List<EpisodeOfCare> episodesOfCare, ReferencedResources referencedResources) {
        return episodesOfCare
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private static UIEpisodeOfCare transform(EpisodeOfCare episodeOfCare, ReferencedResources referencedResources) {

        return new UIEpisodeOfCare()
                .setId(episodeOfCare.getId())
                .setStatus(getStatus(episodeOfCare))
                .setManagingOrganisation(getManagingOrganisation(episodeOfCare, referencedResources))
                .setPeriod(getPeriod(episodeOfCare.getPeriod()))
                .setCareManager(getCareManager(episodeOfCare, referencedResources));
    }

    private static String getStatus(EpisodeOfCare episodeOfCare) {
        if (!episodeOfCare.hasStatus())
            return null;

        return episodeOfCare.getStatus().toCode();
    }

    private static UIOrganisation getManagingOrganisation(EpisodeOfCare episodeOfCare, ReferencedResources referencedResources) {
        if (!episodeOfCare.hasManagingOrganization())
            return null;

        return referencedResources.getUIOrganisation(episodeOfCare.getManagingOrganization());
    }

    private static UIPeriod getPeriod(Period period) {
        UIPeriod uiPeriod = new UIPeriod();
        if (period.hasStart())
            uiPeriod.setStart(period.getStart());

        if (period.hasEnd())
            uiPeriod.setEnd(period.getEnd());

        return uiPeriod;
    }

    private static UIPractitioner getCareManager(EpisodeOfCare episodeOfCare, ReferencedResources referencedResources) {
        if (!episodeOfCare.hasCareManager())
            return null;

        return referencedResources.getUIPractitioner(episodeOfCare.getCareManager());
    }

    public List<Reference> getReferences(List<EpisodeOfCare> episodesOfCare) {
        return StreamExtension.concat(
            episodesOfCare
                        .stream()
                        .filter(EpisodeOfCare::hasPatient)
                        .map(EpisodeOfCare::getPatient),
            episodesOfCare
                        .stream()
                        .filter(EpisodeOfCare::hasManagingOrganization)
                        .map(EpisodeOfCare::getManagingOrganization),
            episodesOfCare
                        .stream()
                        .filter(EpisodeOfCare::hasCareManager)
                        .map(EpisodeOfCare::getCareManager))
            .collect(Collectors.toList());
    }
}
