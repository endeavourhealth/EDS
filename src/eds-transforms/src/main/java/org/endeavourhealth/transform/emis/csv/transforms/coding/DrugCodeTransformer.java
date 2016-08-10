package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.ThreadPool;
import org.endeavourhealth.transform.emis.csv.schema.coding.DrugCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;

import java.util.concurrent.Callable;

public class DrugCodeTransformer {


    public static void transform(String folderPath,
                               CSVFormat csvFormat,
                               CsvProcessor csvProcessor,
                               EmisCsvHelper csvHelper) throws Exception {

        //inserting the entries into the IdCodeMap table is a lot slower than the rest of this
        //file, so split up the saving over a few threads
        ThreadPool threadPool = new ThreadPool(5);

        DrugCode parser = new DrugCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, csvProcessor, csvHelper, threadPool);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
            threadPool.waitAndStop();
        }
    }

    private static void transform(DrugCode drugParser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper,
                                  ThreadPool threadPool) throws Exception {

        final Long codeId = drugParser.getCodeId();
        final String term = drugParser.getTerm();
        final Long dmdId = drugParser.getDmdProductCodeId();

        final CodeableConcept fhirConcept;
        if (dmdId == null) {
            //if there's no DM+D ID, create a textual codeable concept for the term
            fhirConcept = CodeableConceptHelper.createCodeableConcept(term);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, dmdId.toString());
        }

        threadPool.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                csvHelper.addMedication(codeId, fhirConcept, dmdId, term, csvProcessor);
                return null;
            }
        });
    }
}
