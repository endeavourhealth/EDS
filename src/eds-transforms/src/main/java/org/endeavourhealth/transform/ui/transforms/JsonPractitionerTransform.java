package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.models.JsonPractitioner;
import org.hl7.fhir.instance.model.Practitioner;

class JsonPractitionerTransform {
    public static JsonPractitioner transform(Practitioner practitioner) {

        return new JsonPractitioner();
    }
}
