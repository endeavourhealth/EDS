package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.SnomedDalI;
import org.endeavourhealth.core.database.dal.reference.models.SnomedLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

public class SnomedUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(SnomedUpdater.class);

    public static final CSVFormat CSV_FORMAT = CSVFormat.TDF
            .withHeader()
            .withEscape((Character)null)
            .withQuote((Character)null)
            .withQuoteMode(QuoteMode.MINIMAL); //ideally want Quote Mdde NONE, but validation in the library means we need to use this

    /**
     * updates the snomed_lookup and snomed_description_link reference tables from TRUD files
     *
     * Usage
     * =================================================================================
     * 1. Download the latest Snomed release from TRUD (if using the latest delta extract, be sure we've not missed any other deltas, otherwise use the full extract)
     * 2. Then run this utility as:
     *      Main snomed <zip file name and path>
     */
    public static void updateSnomedConceptsAndDescriptions(String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: snomed <root of zipped Snomed release>");
            return;
        }

        LOG.info("SNOMED CT Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
            return;
        }
        if (!ZipHelper.isZip(file)) {
            LOG.error("" + file + " isn't a zip file");
            return;
        }

        ThreadPool threadPool = new ThreadPool(5, 1000);

        //find international concepts file
        LOG.info("Doing International Snomed CT Files");
        ZipInputStream zis = ZipHelper.createZipInputStream(file);
        Reader r = ZipHelper.findFile(zis, "SnomedCT_InternationalRF2_PRODUCTION_.*/Full/Terminology/sct2_Description_Full-en_INT_.*.txt");

        //if we didn't find a "full" version, look for a delta one
        if (r == null) {
            zis.close(); //have to close and re-open
            zis = ZipHelper.createZipInputStream(file);
            r = ZipHelper.findFile(zis, "SnomedCT_InternationalRF2_PRODUCTION_.*/Delta/Terminology/sct2_Description_Delta-en_INT_.*.txt");
        }

        if (r != null) {
            updateConcepts(r, threadPool);
        } else {
            throw new Exception("Failed to find international description file");
        }
        zis.close();

        //find UK concepts file
        LOG.info("Doing UK Snomed CT Files");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "SnomedCT_UKClinicalRF2_PRODUCTION_.*/Full/Terminology/sct2_Description_Full-en_GB1000000_.*.txt");

        //if we didn't find a "full" version, look for a delta one
        if (r == null) {
            zis.close(); //have to close and re-open
            zis = ZipHelper.createZipInputStream(file);
            r = ZipHelper.findFile(zis, "SnomedCT_UKClinicalRF2_PRODUCTION_.*/Delta/Terminology/sct2_Description_Delta-en_GB1000000_.*.txt");
        }

        if (r != null) {
            updateConcepts(r, threadPool);
        } else {
            throw new Exception("Failed to find UK description file");
        }
        zis.close();

        List<ThreadPoolError> errors = threadPool.waitAndStop();
        handleErrors(errors);

        LOG.info("Finished SNOMED CT Update");
    }



    private static void updateConcepts(Reader r, ThreadPool threadPool) throws Exception {

        //save the description -> concept mappings as we go along, in batches
        Map<String, String> descriptionBatch = new HashMap<>();
        int descriptionsDone = 0;

        //the concept -> term mappings can only be saved at the end
        Map<String, String> conceptTypeCache = new HashMap<>();
        Map<String, String> conceptTermCache = new HashMap<>();

        CSVParser parser = new CSVParser(r, CSV_FORMAT);
        int recordNum = 0;
        String lastDescriptionId = null;

        try {
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                recordNum++;
                CSVRecord record = iterator.next();

                String descriptionId = record.get("id");
                String conceptId = record.get("conceptId");
                String typeId = record.get("typeId");
                String term = record.get("term");

                //save the description to concept mapping
                descriptionBatch.put(descriptionId, conceptId);
                saveDescriptionMappings(descriptionBatch, threadPool, false);
                descriptionsDone++;
                if (descriptionsDone % 5000 == 0) {
                    LOG.info("Saved " + descriptionsDone + " description->concept mappings");
                }

                //typeId 900000000000003001 -> Fully specified name
                //typeId 900000000000013009 -> Synonym

                //see if this term is "better" than any other term for the same concept
                String termAlreadyFound = conceptTermCache.get(conceptId);
                String typeAlreadyFound = conceptTypeCache.get(conceptId);
                boolean replace = false;
                if (termAlreadyFound == null) {
                    replace = true;
                } else {
                    //if the type in the map is a synonym, and ours isn't, then we replace it
                    if (typeId.equals("900000000000003001")
                            && !typeAlreadyFound.equals("900000000000003001")) {
                        replace = true;

                        //if the type in the map has the same type but our term is longer, then replace it
                    } else if (typeId.equals(typeAlreadyFound)
                            && term.length() > termAlreadyFound.length()) {
                        replace = true;
                    }
                }

                if (replace) {
                    conceptTermCache.put(conceptId, term);
                    conceptTypeCache.put(conceptId, typeId);
                }

                lastDescriptionId = descriptionId;
            }

        } catch (Exception ex) {
            LOG.error("Error on line " + recordNum);
            LOG.error("Last description ID processed was " + lastDescriptionId);
            throw ex;

        } finally {
            parser.close();
        }

        LOG.info("Saved " + descriptionsDone + " description->concept mappings");
        saveDescriptionMappings(descriptionBatch, threadPool, true);

        //and start saving the concept IDs themselves
        List<SnomedLookup> conceptBatch = new ArrayList<>();
        int conceptsDone = 0;

        for (String conceptId: conceptTermCache.keySet()) {
            String term = conceptTermCache.get(conceptId);
            String typeId = conceptTypeCache.get(conceptId);

            SnomedLookup l = new SnomedLookup();
            l.setConceptId(conceptId);
            l.setTerm(term);
            l.setTypeId(typeId);

            conceptBatch.add(l);
            conceptsDone ++;
            saveConceptMappings(conceptBatch, threadPool, false);
            if (conceptsDone % 5000 == 0) {
                LOG.info("Saved " + conceptsDone + " concept->term mappings");
            }
        }

        LOG.info("Saved " + conceptsDone + " concept->term mappings");
        saveConceptMappings(conceptBatch, threadPool, true);

        threadPool.waitUntilEmpty();


    }

    private static void saveConceptMappings(List<SnomedLookup> batch, ThreadPool threadPool, boolean lastOne) throws Exception {
        if (batch.isEmpty()
            || (!lastOne && batch.size() < 10)) {
            return;
        }

        SaveConceptMappingsCallable callable = new SaveConceptMappingsCallable(new ArrayList<>(batch));
        batch.clear();

        List<ThreadPoolError> errors = threadPool.submit(callable);
        handleErrors(errors);
    }

    private static void saveDescriptionMappings(Map<String, String> batch, ThreadPool threadPool, boolean lastOne) throws Exception {
        if (batch.isEmpty()
                || (!lastOne && batch.size() < 10)) {
            return;
        }

        SaveDescriptionMappingsCallable callable = new SaveDescriptionMappingsCallable(new HashMap<>(batch));
        batch.clear();

        List<ThreadPoolError> errors = threadPool.submit(callable);
        handleErrors(errors);
    }




    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        Throwable cause = first.getException();
        //the cause may be an Exception or Error so we need to explicitly
        //cast to the right type to throw it without changing the method signature
        if (cause instanceof Exception) {
            throw (Exception)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        }
    }

    static class SaveConceptMappingsCallable implements Callable {

        private List<SnomedLookup> objs;

        public SaveConceptMappingsCallable(List<SnomedLookup> objs) {
            this.objs = objs;
        }


        @Override
        public Object call() throws Exception {

            try {
                SnomedDalI dal = DalProvider.factorySnomedDal();
                dal.saveSnomedConcepts(objs);

            } catch (Throwable t) {
                LOG.error("", t);
                throw t;
            }

            return null;
        }
    }

    static class SaveDescriptionMappingsCallable implements Callable {

        private Map<String, String> mappings;

        public SaveDescriptionMappingsCallable(Map<String, String> mappings) {
            this.mappings = mappings;
        }


        @Override
        public Object call() throws Exception {

            try {
                SnomedDalI dal = DalProvider.factorySnomedDal();
                dal.saveSnomedDescriptionToConceptMappings(mappings);

            } catch (Throwable t) {
                LOG.error("", t);
                throw t;
            }

            return null;
        }
    }

}
