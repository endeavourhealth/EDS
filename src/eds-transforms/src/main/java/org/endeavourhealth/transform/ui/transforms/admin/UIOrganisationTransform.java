package org.endeavourhealth.transform.ui.transforms.admin;

import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.hl7.fhir.instance.model.Organization;

public class UIOrganisationTransform {
    public static UIOrganisation transform(Organization organization) {
        return new UIOrganisation()
                .setId(organization.getId())
                .setName(organization.getName())
                .setType(organization.getType().getText());
    }
}
