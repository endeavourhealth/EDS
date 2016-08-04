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
import org.endeavourhealth.transform.terminology.Snomed;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ClinicalCodeTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ClinicalCodeTransformer.class);

    public static void transform(String folderPath,
                               CSVFormat csvFormat,
                               CsvProcessor csvProcessor,
                               EmisCsvHelper csvHelper) throws Exception {

        //because we have to hit a third party web resource, we use a thread pool to support
        //threading these calls to improve performance
        ExecutorService threadPool = Executors.newFixedThreadPool(15); //seems suitable for concurrent hits against a web service
        AtomicInteger threadPoolQueueSize = new AtomicInteger();

        Coding_ClinicalCode parser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, csvProcessor, csvHelper, threadPool, threadPoolQueueSize);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

        //close and let our thread pool finish
        threadPool.shutdown();
        try {

            while (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
                LOG.trace("Waiting for {} clinical codes to be looked up", threadPoolQueueSize.get());
            }

        } catch (InterruptedException ex) {
            LOG.error("Thread interrupted", ex);
        }
    }

    private static void transform(Coding_ClinicalCode codeParser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper,
                                  ExecutorService threadPool,
                                  AtomicInteger threadPoolQueueSize) throws Exception {

        Long codeId = codeParser.getCodeId();

        String emisTerm = codeParser.getTerm();
        String emisCode = codeParser.getReadTermId();
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

        threadPoolQueueSize.incrementAndGet();
        threadPool.submit(new WebServiceLookup(codeId, fhirConcept, codeType, snomedConceptId,
                                                snomedDescriptionId, csvProcessor, csvHelper, threadPoolQueueSize));
    }

    static class WebServiceLookup implements Callable {

        private Long codeId = null;
        private CodeableConcept fhirConcept = null;
        private ClinicalCodeType codeType = null;
        private Long snomedConceptId = null;
        private Long snomedDescriptionId = null;
        private CsvProcessor csvProcessor = null;
        private EmisCsvHelper csvHelper = null;
        private AtomicInteger threadPoolQueueSize = null;

        public WebServiceLookup(Long codeId,
                                CodeableConcept fhirConcept,
                                ClinicalCodeType codeType,
                                Long snomedConceptId,
                                Long snomedDescriptionId,
                                CsvProcessor csvProcessor,
                                EmisCsvHelper csvHelper,
                                AtomicInteger threadPoolQueueSize) {

            this.codeId = codeId;
            this.fhirConcept = fhirConcept;
            this.codeType = codeType;
            this.snomedConceptId = snomedConceptId;
            this.snomedDescriptionId = snomedDescriptionId;
            this.csvProcessor = csvProcessor;
            this.csvHelper = csvHelper;
            this.threadPoolQueueSize = threadPoolQueueSize;
        }

        @Override
        public Object call() throws Exception {

            try {
                String snomedTerm = Snomed.getTerm(snomedConceptId.longValue(), snomedDescriptionId.longValue());
                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
            } catch (Exception ex) {
                //if we didn't get a term for the IDs, then it was a local term, so even though the Snomed code
                //may have been non-null, the mapping wasn't to a valid Snomed concept/term pair, so don't add it to the FHIR resource
            }

            csvHelper.addClinicalCode(codeId, fhirConcept, codeType, csvProcessor);

            threadPoolQueueSize.decrementAndGet();
            return null;
        }
    }
}
