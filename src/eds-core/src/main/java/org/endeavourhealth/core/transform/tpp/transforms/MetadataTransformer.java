package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.tpp.schema.Metadata;
import org.endeavourhealth.core.transform.tpp.schema.Site;
import org.endeavourhealth.core.transform.tpp.schema.User;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class MetadataTransformer {

    public static void transform(Metadata tppMetadata, List<Resource> fhirResources) {

        for (Site site : tppMetadata.getSite()) {
            SiteTransformer.transform(site, fhirResources);
        }

        for (User user : tppMetadata.getUser()) {
            UserTransformer.transform(user, fhirResources);
        }

        linkOrganisations(fhirResources);
    }

    private static void linkOrganisations(List<Resource> fhirResources) {

        //the first site is the "main site" that the following ones belong to
        //e.g. the GP practice and then branch sites
        //so link the subsequent ones to the first
        List<Organization> fhirOrganisations = fhirResources
                .stream()
                .filter(t -> t instanceof Organization)
                .map(t -> (Organization)t)
                .collect(Collectors.toList());

        if (fhirOrganisations.isEmpty()) {
            return;
        }

        Organization firstOrg = fhirOrganisations.remove(0);
        String id = firstOrg.getId();

        for (Organization org: fhirOrganisations) {
            org.setPartOf(Fhir.createReference(ResourceType.Organization, id));
        }

        //link all the users to the organisation as well
        List<Practitioner> fhirPractitioners = fhirResources
                .stream()
                .filter(t -> t instanceof Practitioner)
                .map(t -> (Practitioner)t)
                .collect(Collectors.toList());

        for (Practitioner fhirPractitioner: fhirPractitioners) {
            List<Practitioner.PractitionerPractitionerRoleComponent> fhirRoles = fhirPractitioner.getPractitionerRole();
            for (Practitioner.PractitionerPractitionerRoleComponent fhirRole: fhirRoles) {
                fhirRole.setManagingOrganization(Fhir.createReference(ResourceType.Organization, id));
            }

        }

    }
}
