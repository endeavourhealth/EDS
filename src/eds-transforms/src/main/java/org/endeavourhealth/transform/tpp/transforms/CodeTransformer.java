package org.endeavourhealth.transform.tpp.transforms;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.tpp.schema.Code;
import org.endeavourhealth.transform.tpp.schema.CodeScheme;
import org.hl7.fhir.instance.model.CodeableConcept;

public class CodeTransformer {

    public static CodeableConcept transform(Code tppCode)  throws TransformException {

        String code = tppCode.getCode();
        CodeScheme scheme = tppCode.getScheme();
        String term = tppCode.getTerm();

        if (scheme == CodeScheme.CTV_3) {
            return CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, code);
        } else if (scheme == CodeScheme.SNOMED) {
            return CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_CTV3, term, code);
        } else {
            throw new TransformException("Unsupported code scheme " + scheme);
        }

    }
}
