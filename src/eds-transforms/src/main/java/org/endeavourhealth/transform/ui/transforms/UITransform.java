package org.endeavourhealth.transform.ui.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.endeavourhealth.transform.ui.models.resources.*;
import org.endeavourhealth.transform.ui.transforms.admin.UIPatientTransform;
import org.endeavourhealth.transform.ui.transforms.clinical.*;
import org.hl7.fhir.instance.model.*;

public class UITransform {
    public static UIPatient transformPatient(Patient patient) {
        return UIPatientTransform.transform(patient);
    }

    public static <T extends UIResource> UIClinicalTransform getClinicalTransformer(Class<T> resourceType) {

        if (resourceType == UICondition.class)
            return new UIConditionTransform();
        else if (resourceType == UIProblem.class)
            return new UIProblemTransform();
        else if (resourceType == UIEncounter.class)
            return new UIEncounterTransform();
        else if (resourceType == UIObservation.class)
            return new UIObservationTransform();

        throw new NotImplementedException(resourceType.getSimpleName());
    }
}
