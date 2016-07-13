package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.Coding_ClinicalCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.terminology.Snomed;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class ClinicalCodeTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ClinicalCodeTransformer.class);

    public static HashMap<Long, CodeableConcept> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<Long, CodeableConcept> ret = new HashMap<>();

        Coding_ClinicalCode parser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, ret);
            }
        } finally {
            parser.close();
        }

        return ret;
    }

    private static void transform(Coding_ClinicalCode codeParser, HashMap<Long, CodeableConcept> map) throws Exception {

        Long codeId = codeParser.getCodeId();

        String emisTerm = codeParser.getTerm();
        String emisCode = codeParser.getReadTermId();
        String nationalCategory = codeParser.getNationalCodeCategory();
        String nationalCode = codeParser.getNationalCode();
        String nationalDescription = codeParser.getNationalDescription();
        Long snomedConceptId = codeParser.getSnomedCTConceptId();
        Long snomedDescriptionId = codeParser.getSnomedCTDescriptionId();
        String emisCategory = codeParser.getEmisCodeCategoryDescription();

        CodeableConcept fhirConcept = null;

        if (emisCode == null) {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(emisTerm);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);

            try {
                //TODO - need faster way to lookup Snomed terms for concept and desc IDs
                String snomedTerm = emisTerm;
                //String snomedTerm = Snomed.getTerm(snomedConceptId.longValue(), snomedDescriptionId.longValue());
                //TODO - need to validate Snomed concept IDs and remove non-valid ones

                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
            } catch (Exception ex) {
                LOG.error("Failed to find term for Coding_ClinicalCode CodeId: " + codeId + " SnomedConceptId: " +snomedConceptId + " SnomedTermId: " + snomedDescriptionId);
                //throw new TransformException("Failed to find term for Clinical CodeId " + codeId + " SnomedConceptId: " +snomedConceptId + " SnomedTermId: " + snomedDescriptionId, ex);
            }
        }

        map.put(codeId, fhirConcept);
    }

}
