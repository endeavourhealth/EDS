package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.CodingHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.data.admin.CodeRepository;
import org.endeavourhealth.core.data.admin.models.SnomedLookup;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class ClinicalCodeTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ClinicalCodeTransformer.class);

    private static CodeRepository repository = new CodeRepository();

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper,
                                 int maxFilingThreads) throws Exception {

        //because we have to hit a third party web resource, we use a thread pool to support
        //threading these calls to improve performance
        ThreadPool threadPool = new ThreadPool(maxFilingThreads, 50000);

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        try {
            AbstractCsvParser parser = parsers.get(ClinicalCode.class);
            while (parser.nextRecord()) {

                try {
                    transform((ClinicalCode)parser, fhirResourceFiler, csvHelper, threadPool, version);
                } catch (Exception ex) {
                    throw new TransformException(parser.getCurrentState().toString(), ex);
                }
            }

        } finally {
            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);
        }
    }


    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        WebServiceLookup callable = (WebServiceLookup)first.getCallable();
        Exception exception = first.getException();
        CsvCurrentState parserState = callable.getParserState();
        throw new TransformException(parserState.toString(), exception);
    }

    private static void transform(ClinicalCode parser,
                                  FhirResourceFiler fhirResourceFiler,
                                  EmisCsvHelper csvHelper,
                                  ThreadPool threadPool,
                                  String version) throws Exception {

        Long codeId = parser.getCodeId();
        String emisTerm = parser.getTerm();
        String emisCode = parser.getReadTermId();
        Long snomedConceptId = parser.getSnomedCTConceptId();
        Long snomedDescriptionId = parser.getSnomedCTDescriptionId();
        String emisCategory = parser.getEmisCodeCategoryDescription();
        String nationalCode = parser.getNationalCode();
        String nationalCodeCategory = parser.getNationalCodeCategory();
        String nationalCodeDescription = parser.getNationalDescription();

        //the parent code ID was added after 5.3
        Long parentCodeId = null;
        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_4)) {
            parentCodeId = parser.getParentCodeId();
        }

        ClinicalCodeType codeType = ClinicalCodeType.fromValue(emisCategory);

        CodeableConcept fhirConcept = null;

        //the CSV uses a hyphen to delimit the synonym ID from the code, but since we include
        //the original term text anyway, there's no need to carry the synonym ID into the FHIR data
        String emisCodeNoSynonym = emisCode;
        int index = emisCodeNoSynonym.indexOf("-");
        if (index > -1) {
            emisCodeNoSynonym = emisCodeNoSynonym.substring(0, index);
        }

        //without a Read 2 engine, there seems to be no cast-iron way to determine whether the supplied codes
        //are Read 2 codes or Emis local codes. Looking at the codes from the test data sets, this seems
        //to be a reliable way to perform the same check.
        if (emisCode.startsWith("EMIS")
                || emisCode.startsWith("ALLERGY")
                || emisCode.startsWith("EGTON")
                || emisCodeNoSynonym.length() > 5) {

            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_EMIS_CODE, emisTerm, emisCode);

        } else {

            //Emis store Read 2 codes without the padding stops, which seems to be against Read 2 standards,
            //so make sure all codes are padded to five chars
            while (emisCode.length() < 5) {
                emisCode += ".";
            }

            //should ideally be able to distringuish between Read2 and EMIS codes
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);
        }

        //always set the selected term as the text
        fhirConcept.setText(emisTerm);

        //spin the remainder of our work off to a small thread pool, so we can perform multiple snomed term lookups in parallel
        List<ThreadPoolError> errors = threadPool.submit(new WebServiceLookup(parser.getCurrentState(), codeId,
                                                            fhirConcept, codeType, emisTerm,
                                                            emisCode, snomedConceptId, snomedDescriptionId,
                                                            nationalCode, nationalCodeCategory, nationalCodeDescription,
                                                            parentCodeId, csvHelper));
        handleErrors(errors);
    }


    static class WebServiceLookup implements Callable {

        private CsvCurrentState parserState = null;
        private Long codeId = null;
        private CodeableConcept fhirConcept = null;
        private ClinicalCodeType codeType = null;
        private String readTerm = null;
        private String readCode = null;
        private Long snomedConceptId = null;
        private Long snomedDescriptionId = null;
        private String nationalCode = null;
        private String nationalCodeCategory = null;
        private String nationalCodeDescription = null;
        private Long parentCodeId = null;
        private EmisCsvHelper csvHelper = null;

        public WebServiceLookup(CsvCurrentState parserState,
                                Long codeId,
                                CodeableConcept fhirConcept,
                                ClinicalCodeType codeType,
                                String readTerm,
                                String readCode,
                                Long snomedConceptId,
                                Long snomedDescriptionId,
                                String nationalCode,
                                String nationalCodeCategory,
                                String nationalCodeDescription,
                                Long parentCodeId,
                                EmisCsvHelper csvHelper) {

            this.parserState = parserState;
            this.codeId = codeId;
            this.fhirConcept = fhirConcept;
            this.codeType = codeType;
            this.readTerm = readTerm;
            this.readCode = readCode;
            this.snomedConceptId = snomedConceptId;
            this.snomedDescriptionId = snomedDescriptionId;
            this.nationalCode = nationalCode;
            this.nationalCodeCategory = nationalCodeCategory;
            this.nationalCodeDescription = nationalCodeDescription;
            this.parentCodeId = parentCodeId;
            this.csvHelper = csvHelper;
        }

        @Override
        public Object call() throws Exception {

            String snomedTerm = null;

            SnomedLookup snomedLookup = repository.getSnomedLookup(snomedConceptId.toString());

            if (snomedLookup == null) {
                //if the concept ID isn't a valid Snomed concept, then still store in FHIR, but as an EMIS code
                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_EMISSNOMED, readTerm, snomedConceptId.toString()));

            } else {
                snomedTerm = snomedLookup.getTerm();
                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
            }

            //although not supported by FHIR, we should store the description ID we've been given somewhere
            fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_DESCRIPTION_ID, "", snomedDescriptionId.toString()));

            //store the coding in Cassandra
            csvHelper.addClinicalCode(codeId, fhirConcept, codeType, readTerm,
                    readCode, snomedConceptId, snomedDescriptionId, snomedTerm,
                    nationalCode, nationalCodeCategory, nationalCodeDescription, parentCodeId);

            return null;
        }

        public CsvCurrentState getParserState() {
            return parserState;
        }
    }
}
