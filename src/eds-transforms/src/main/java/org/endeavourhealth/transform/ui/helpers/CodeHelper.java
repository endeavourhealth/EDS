package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

import java.util.stream.Collectors;

public class CodeHelper {

    public static UICodeableConcept convert(CodeableConcept codeableConcept) {
        UICodeableConcept uiCodeableConcept = new UICodeableConcept();

        if (codeableConcept != null) {
            uiCodeableConcept.setText(codeableConcept.getText());

            uiCodeableConcept.setCodes(codeableConcept
                    .getCoding()
                    .stream()
                    .map(t -> convert(t))
                    .collect(Collectors.toList()));
        }

        return uiCodeableConcept;
    }

    public static UICode convert(Coding coding) {
        return new UICode()
                .setCode(coding.getCode())
                .setDisplay(coding.getDisplay())
                .setSystem(coding.getSystem());
    }
}
