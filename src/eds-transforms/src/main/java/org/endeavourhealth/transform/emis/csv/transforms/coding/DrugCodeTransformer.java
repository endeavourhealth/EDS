package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.Meta;

import java.io.IOException;
import java.util.HashMap;

public class DrugCodeTransformer {


    public static HashMap<String, Medication> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<String, Medication> ret = new HashMap<>();

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

    private static void transform(Coding_DrugCode drugParser, HashMap<String, Medication> map) {

        Medication fhirMedication = new Medication();
        fhirMedication.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_MEDICATION));

        String codeId = drugParser.getCodeId().toString();
        String term = drugParser.getTerm();
        Long dmdId = drugParser.getDmdProductCodeId();

        //ID is set on the resource when it's copied for use in the object store
        //fhirMedication.setId(codeId);

        if (dmdId == null) {
            fhirMedication.setCode(CodeableConceptHelper.createCodeableConcept(term));
        } else {
            fhirMedication.setCode(CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, dmdId.toString()));
        }

        map.put(codeId, fhirMedication);
    }
}
