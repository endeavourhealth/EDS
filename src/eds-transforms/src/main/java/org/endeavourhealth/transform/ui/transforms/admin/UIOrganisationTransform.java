package org.endeavourhealth.transform.ui.transforms.admin;

import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.hl7.fhir.instance.model.Organization;

public class UIOrganisationTransform {
    public static UIOrganisation transform(Organization organization) {
        return new UIOrganisation()
                .setId(organization.getId())
                .setName(organization.getName())
                .setType(getOrganizationType(organization));
    }

    public static String getOrganizationType(Organization organization) {
			if (!organization.hasType())
				return null;

			if (organization.getType().hasText())
    		return organization.getType().getText();

    	if (!organization.getType().hasCoding() || organization.getType().getCoding().size() == 0)
    		return null;

    	return organization.getType().getCoding().get(0).getCode();
		}
}
