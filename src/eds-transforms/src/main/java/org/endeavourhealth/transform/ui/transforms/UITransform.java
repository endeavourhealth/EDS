package org.endeavourhealth.transform.ui.transforms;

import org.apache.commons.lang3.NotImplementedException;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UICondition;
import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.endeavourhealth.transform.ui.models.UIPatient;
import org.endeavourhealth.transform.ui.models.UIPractitioner;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UITransform {
    public static UIPatient transformPatient(Patient patient) {
        return UIPatientTransform.transform(patient);
    }

    public static <T extends Resource> IUIClinicalTransform getClinicalTransformer(Class<T> resourceType) {

        if (resourceType == Condition.class)
            return new UIConditionTransform();
        else if (resourceType == Encounter.class)
            return new UIEncounterTransform();

        throw new NotImplementedException(resourceType.getSimpleName());
    }
}
