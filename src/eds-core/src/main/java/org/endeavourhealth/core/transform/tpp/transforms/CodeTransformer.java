package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.common.TransformException;
import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.Code;
import org.endeavourhealth.core.transform.tpp.schema.CodeScheme;
import org.hl7.fhir.instance.model.CodeableConcept;

public class CodeTransformer {

    public static CodeableConcept transform(Code tppCode) {

        String code = tppCode.getCode();
        CodeScheme scheme = tppCode.getScheme();
        String term = tppCode.getTerm();

        if (scheme == CodeScheme.CTV_3) {
            return Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_SNOMED_CT, term, code);
        } else if (scheme == CodeScheme.SNOMED) {
            return Fhir.createCodeableConcept(FhirUris.CODE_SYSTEM_CTV3, term, code);
        } else {
            throw new TransformException("Unsupported code scheme " + scheme);
        }

    }
}
