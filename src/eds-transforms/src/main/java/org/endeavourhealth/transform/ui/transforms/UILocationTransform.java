package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.models.UILocation;
import org.hl7.fhir.instance.model.Location;

public class UILocationTransform {
    public static UILocation transform(Location location) {
        return new UILocation();
    }
}
