package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.ClinicalCodeType;
import org.endeavourhealth.transform.emis.csv.schema.Coding_ClinicalCode;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ClinicalCodeTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ClinicalCodeTransformer.class);

    public static void transform(String folderPath,
                               CSVFormat csvFormat,
                               CsvProcessor csvProcessor,
                               EmisCsvHelper csvHelper) throws Exception {

        //because we have to hit a third party web resource, we use a thread pool to support
        //threading these calls to improve performance
        ExecutorService threadPool = Executors.newFixedThreadPool(15);

        Coding_ClinicalCode parser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, csvProcessor, csvHelper, threadPool);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

        //close and let our thread pool finish
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            LOG.error("Thread interrupted", ex);
        }

    }

    private static void transform(Coding_ClinicalCode codeParser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper,
                                  ExecutorService threadPool) throws Exception {

        Long codeId = codeParser.getCodeId();

        String emisTerm = codeParser.getTerm();
        String emisCode = codeParser.getReadTermId();
        String nationalCategory = codeParser.getNationalCodeCategory();
        String nationalCode = codeParser.getNationalCode();
        String nationalDescription = codeParser.getNationalDescription();
        Long snomedConceptId = codeParser.getSnomedCTConceptId();
        Long snomedDescriptionId = codeParser.getSnomedCTDescriptionId();
        String emisCategory = codeParser.getEmisCodeCategoryDescription();

        ClinicalCodeType codeType = ClinicalCodeType.fromValue(emisCategory);

        CodeableConcept fhirConcept = null;

        if (emisCode == null) {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(emisTerm);
        } else {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);
        }

        threadPool.submit(new WebServiceLookup(codeId, fhirConcept, codeType, snomedConceptId,
                                                snomedDescriptionId, csvProcessor, csvHelper));
    }

    static class WebServiceLookup implements Callable {

        private Long codeId = null;
        private CodeableConcept fhirConcept = null;
        private ClinicalCodeType codeType = null;
        private Long snomedConceptId = null;
        private Long snomedDescriptionId = null;
        private CsvProcessor csvProcessor = null;
        private EmisCsvHelper csvHelper = null;

        public WebServiceLookup(Long codeId,
                                CodeableConcept fhirConcept,
                                ClinicalCodeType codeType,
                                Long snomedConceptId,
                                Long snomedDescriptionId,
                                CsvProcessor csvProcessor,
                                EmisCsvHelper csvHelper) {

            this.codeId = codeId;
            this.fhirConcept = fhirConcept;
            this.codeType = codeType;
            this.snomedConceptId = snomedConceptId;
            this.snomedDescriptionId = snomedDescriptionId;
            this.csvProcessor = csvProcessor;
            this.csvHelper = csvHelper;
        }

        @Override
        public Object call() throws Exception {

            //LOG.trace("Looking up for " + snomedConceptId);
            try {
                //TODO - restore snomed lookup
                String snomedTerm = "MISSING";
                //String snomedTerm = Snomed.getTerm(snomedConceptId.longValue(), snomedDescriptionId.longValue());
                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
            } catch (Exception ex) {
                //if we didn't get a term for the IDs, then it was a local term, so even though the Snomed code
                //may have been non-null, the mapping wasn't to a valid Snomed concept/term pair, so don't add it to the FHIR resource
            }

            csvHelper.addClinicalCode(codeId, fhirConcept, codeType, csvProcessor);
            //LOG.trace("    Finished up for " + snomedConceptId);
            return null;
        }
    }
}
