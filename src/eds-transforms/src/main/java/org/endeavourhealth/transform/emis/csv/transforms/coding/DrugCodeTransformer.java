package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.Meta;

import java.io.IOException;
import java.util.HashMap;

public class DrugCodeTransformer {


    public static HashMap<Long, CodeableConcept> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<Long, CodeableConcept> ret = new HashMap<>();

        Coding_DrugCode parser = new Coding_DrugCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, ret);
            }
        } finally {
            parser.close();
        }

        return ret;
    }

    private static void transform(Coding_DrugCode drugParser, HashMap<Long, CodeableConcept> map) {

        Long codeId = drugParser.getCodeId();
        String term = drugParser.getTerm();
        Long dmdId = drugParser.getDmdProductCodeId();

        CodeableConcept fhirConcept = null;
        if (dmdId == null) {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(term);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, dmdId.toString());
        }

        map.put(codeId, fhirConcept);
    }
}
