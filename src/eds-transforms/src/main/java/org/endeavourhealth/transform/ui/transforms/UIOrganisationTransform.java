package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.models.resources.UIOrganisation;
import org.hl7.fhir.instance.model.Organization;

public class UIOrganisationTransform {
    public static UIOrganisation transform(Organization organization) {
        return new UIOrganisation();
    }
}
