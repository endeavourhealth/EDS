package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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
        List<Future> futures = new ArrayList<>();

        ClinicalCode parser = new ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, csvProcessor, csvHelper, threadPool, threadPoolQueueSize, futures);
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

        //check the futures to see if any exceptions were raised
        checkFutures(futures);

    }

    private static void transform(ClinicalCode parser,
                                  CsvProcessor csvProcessor,
                                  EmisCsvHelper csvHelper,
                                  ExecutorService threadPool,
                                  AtomicInteger threadPoolQueueSize,
                                  List<Future> futures) throws Exception {

        Long codeId = parser.getCodeId();
        String emisTerm = parser.getTerm();
        String emisCode = parser.getReadTermId();
        Long snomedConceptId = parser.getSnomedCTConceptId();
        Long snomedDescriptionId = parser.getSnomedCTDescriptionId();
        String emisCategory = parser.getEmisCodeCategoryDescription();
        String nationalCode = parser.getNationalCode();
        String nationalCodeCategory = parser.getNationalCodeCategory();
        String nationalCodeDescription = parser.getNationalDescription();

        ClinicalCodeType codeType = ClinicalCodeType.fromValue(emisCategory);

        CodeableConcept fhirConcept = null;

        if (emisCode == null) {
            fhirConcept = CodeableConceptHelper.createCodeableConcept(emisTerm);
        } else {
            //should ideally be able to distringuish between Read2 and EMIS codes
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);
        }

        threadPoolQueueSize.incrementAndGet();
        Future<?> future = threadPool.submit(new WebServiceLookup(codeId, fhirConcept, codeType, emisTerm,
                                            emisCode, snomedConceptId, snomedDescriptionId,
                                            nationalCode, nationalCodeCategory, nationalCodeDescription,
                                            csvProcessor, csvHelper, threadPoolQueueSize));
        futures.add(future);

        //every time we've added a number of futures, just have a check to see if any are outstanding, which will also raise
        //any exceptions generated from the callables
        if (futures.size() % 1000 == 0) {
            checkFutures(futures);
        }

    }

    private static void checkFutures(List<Future> futures) throws Exception {

        //iterate in reverse, so we are safe to remove
        for (int i=futures.size()-1; i>=0; i--) {
            Future<?> future = futures.get(i);
            if (future.isDone()) {
                try {
                    //just calling get on the future will cause any exception to be raised in this thread
                    future.get();
                    futures.remove(i);
                } catch (Exception ex) {
                    throw (Exception)ex.getCause();
                }
            }
        }
    }

    static class WebServiceLookup implements Callable {

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
        private CsvProcessor csvProcessor = null;
        private EmisCsvHelper csvHelper = null;
        private AtomicInteger threadPoolQueueSize = null;

        public WebServiceLookup(Long codeId,
                                CodeableConcept fhirConcept,
                                ClinicalCodeType codeType,
                                String readTerm,
                                String readCode,
                                Long snomedConceptId,
                                Long snomedDescriptionId,
                                String nationalCode,
                                String nationalCodeCategory,
                                String nationalCodeDescription,
                                CsvProcessor csvProcessor,
                                EmisCsvHelper csvHelper,
                                AtomicInteger threadPoolQueueSize) {

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
            this.csvProcessor = csvProcessor;
            this.csvHelper = csvHelper;
            this.threadPoolQueueSize = threadPoolQueueSize;
        }

        @Override
        public Object call() throws Exception {

            String snomedTerm = null;

            //TODO - restored snomed term lookip - FAR TOO SLOW - need locally held mappings
            /*try {
                snomedTerm = Snomed.getTerm(snomedConceptId.longValue(), snomedDescriptionId.longValue());
                fhirConcept.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
            } catch (Exception ex) {
                //if we didn't get a term for the IDs, then it was a local term, so even though the Snomed code
                //may have been non-null, the mapping wasn't to a valid Snomed concept/term pair, so don't add it to the FHIR resource
            }*/

            csvHelper.addClinicalCode(codeId, fhirConcept, codeType, readTerm,
                    readCode, snomedConceptId, snomedDescriptionId, snomedTerm,
                    nationalCode, nationalCodeCategory, nationalCodeDescription, csvProcessor);

            threadPoolQueueSize.decrementAndGet();
            return null;
        }
    }
}
