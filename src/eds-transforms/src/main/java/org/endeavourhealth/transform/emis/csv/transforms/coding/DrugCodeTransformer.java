package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.coding.DrugCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;

public class DrugCodeTransformer {


    public static void transform(String folderPath,
                               CSVFormat csvFormat,
                               CsvProcessor csvProcessor,
                               EmisCsvHelper csvHelper) throws Exception {

        DrugCode parser = new DrugCode(folderPath, csvFormat);
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

    private static void transform(DrugCode drugParser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper) throws Exception {

        Long codeId = drugParser.getCodeId();
        String term = drugParser.getTerm();
        Long dmdId = drugParser.getDmdProductCodeId();

        CodeableConcept fhirConcept = null;
        if (dmdId == null) {
            //if there's no DM+D ID, create a textual codeable concept for the term
            fhirConcept = CodeableConceptHelper.createCodeableConcept(term);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, dmdId.toString());
        }

        csvHelper.addMedication(codeId, fhirConcept, dmdId, term, csvProcessor);
    }
}
