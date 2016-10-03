package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.transform.ui.models.UICode;
import org.endeavourhealth.transform.ui.models.UICodeableConcept;
import org.hl7.fhir.instance.model.CodeableConcept;

import java.util.stream.Collectors;

public class CodeHelper {
    public static UICodeableConcept convert(CodeableConcept codeableConcept) {
        UICodeableConcept uiCodeableConcept = new UICodeableConcept();

        if (codeableConcept != null) {
            uiCodeableConcept.setText(codeableConcept.getText());

            uiCodeableConcept.setCodes(codeableConcept
                    .getCoding()
                    .stream()
                    .map(t -> new UICode()
                            .setCode(t.getCode())
                            .setDisplay(t.getDisplay())
                            .setSystem(t.getSystem()))
                    .collect(Collectors.toList()));
        }

        return uiCodeableConcept;
    }
}
