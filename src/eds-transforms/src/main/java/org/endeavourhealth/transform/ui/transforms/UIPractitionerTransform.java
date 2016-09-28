package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.helpers.NameHelper;
import org.endeavourhealth.transform.ui.models.UIPractitioner;
import org.hl7.fhir.instance.model.Practitioner;

class UIPractitionerTransform {
    public static UIPractitioner transform(Practitioner practitioner) {

        return new UIPractitioner()
                .setDisplayName(NameHelper.getNameForDisplay(practitioner.getName()))
                .setActive(practitioner.getActive());
    }
}
