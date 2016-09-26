package org.endeavourhealth.ui.business.recordViewer.transforms;

import org.endeavourhealth.ui.business.recordViewer.models.JsonPractitioner;
import org.hl7.fhir.instance.model.Practitioner;

public class JsonPractitionerTransform {
    public JsonPractitioner transform(Practitioner practitioner) {

        return new JsonPractitioner();
    }
}
