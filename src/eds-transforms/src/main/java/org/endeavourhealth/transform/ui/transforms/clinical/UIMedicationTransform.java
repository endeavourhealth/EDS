package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedication;
import org.hl7.fhir.instance.model.Medication;

public class UIMedicationTransform {
    public static UIMedication transform(Medication medication) {

        return new UIMedication()
                .setId(medication.getId())
                .setCode(CodeHelper.convert(medication.getCode()));
    }
}
