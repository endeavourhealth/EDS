package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.ClinicalCodeType;
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

    public static void transform(String folderPath,
                               CSVFormat csvFormat,
                               CsvProcessor csvProcessor,
                               EmisCsvHelper csvHelper) throws Exception {

        Coding_ClinicalCode parser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, csvProcessor, csvHelper);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void transform(Coding_ClinicalCode codeParser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper) throws Exception {

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

        ClinicalCodeType codeType = ClinicalCodeType.fromValue(emisCategory);

        csvHelper.addClinicalCode(codeId, fhirConcept, codeType, csvProcessor);
   }

}
