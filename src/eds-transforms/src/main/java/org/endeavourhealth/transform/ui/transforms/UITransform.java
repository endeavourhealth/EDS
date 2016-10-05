package org.endeavourhealth.transform.ui.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.clinicial.*;
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
        else if (resourceType == UIAllergyIntolerance.class)
            return new UIAllergyIntoleranceTransform();
        else if (resourceType == UIImmunisation.class)
            return new UIImmunisationTransform();
        else if (resourceType == UIProcedure.class)
            return new UIProcedureTransform();

        throw new NotImplementedException(resourceType.getSimpleName());
    }
}
