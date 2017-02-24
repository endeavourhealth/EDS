package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.coding.DrugCode;
import org.hl7.fhir.instance.model.CodeableConcept;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class DrugCodeTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper,
                                 int maxFilingThreads) throws Exception {

        //inserting the entries into the IdCodeMap table is a lot slower than the rest of this
        //file, so split up the saving over a few threads
        ThreadPool threadPool = new ThreadPool(maxFilingThreads, 50000);

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        try {
            AbstractCsvParser parser = parsers.get(DrugCode.class);
            while (parser.nextRecord()) {

                try {
                    transform((DrugCode)parser, fhirResourceFiler, csvHelper, threadPool);
                } catch (Exception ex) {
                    throw new TransformException(parser.getCurrentState().toString(), ex);
                }
            }

        } finally {
            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);
        }
    }

    private static void transform(DrugCode parser,
                                  FhirResourceFiler fhirResourceFiler,
                                  EmisCsvHelper csvHelper,
                                  ThreadPool threadPool) throws Exception {

        final Long codeId = parser.getCodeId();
        final String term = parser.getTerm();
        final Long dmdId = parser.getDmdProductCodeId();

        final CodeableConcept fhirConcept;
        if (dmdId == null) {
            //if there's no DM+D ID, create a textual codeable concept for the term
            fhirConcept = CodeableConceptHelper.createCodeableConcept(term);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_SNOMED_CT, term, dmdId.toString());
        }

        //always set the selected term as the text
        fhirConcept.setText(term);

        List<ThreadPoolError> errors = threadPool.submit(new DrugSaveCallable(parser.getCurrentState(), csvHelper, codeId, fhirConcept, dmdId, term));
        handleErrors(errors);
    }

    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        DrugSaveCallable callable = (DrugSaveCallable)first.getCallable();
        Exception exception = first.getException();
        CsvCurrentState parserState = callable.getParserState();
        throw new TransformException(parserState.toString(), exception);
    }

    static class DrugSaveCallable implements Callable {

        private CsvCurrentState parserState = null;
        private EmisCsvHelper csvHelper = null;
        private Long codeId = null;
        private CodeableConcept fhirConcept = null;
        private Long dmdId = null;
        private String term = null;

        public DrugSaveCallable(CsvCurrentState parserState,
                                EmisCsvHelper csvHelper,
                                Long codeId,
                                CodeableConcept fhirConcept,
                                Long dmdId,
                                String term) {

            this.parserState = parserState;
            this.csvHelper = csvHelper;
            this.codeId = codeId;
            this.fhirConcept = fhirConcept;
            this.dmdId = dmdId;
            this.term = term;
        }

        @Override
        public Object call() throws Exception {
            csvHelper.addMedication(codeId, fhirConcept, dmdId, term);
            return null;
        }

        public CsvCurrentState getParserState() {
            return parserState;
        }
    }
}
