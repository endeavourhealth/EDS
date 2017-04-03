package org.endeavourhealth.transform.ui.transforms.admin;

import org.endeavourhealth.transform.ui.models.resources.admin.UILocation;
import org.hl7.fhir.instance.model.Location;

public class UILocationTransform {
    public static UILocation transform(Location location) {
        return new UILocation()
                .setId(location.getId())
								.setName(location.getName())
								.setDescription(location.getDescription());
    }
}
