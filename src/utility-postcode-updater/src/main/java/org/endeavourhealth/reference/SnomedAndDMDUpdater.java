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
import org.endeavourhealth.core.database.rdbms.DeadlockHandler;
import org.endeavourhealth.reference.helpers.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

public class SnomedAndDMDUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(SnomedAndDMDUpdater.class);

    private static final String TYPE_FULLY_SPECIFIED_NAME = "900000000000003001";
    private static final String TYPE_SYNONYM = "900000000000013009";

    private static final String MODULE_UK_EDITION = "999000041000000102";
    private static final String MODULE_UK_CLINICAL_EXTENSION_REFERENCE_SET = "999000021000000109";
    private static final String MODULE_UK_CLINICAL_EXTENSION = "999000011000000103";
    private static final String MODULE_UK_DRUG_EXTENSION = "999000011000001104";
    private static final String MODULE_UK_DRUG_EXTENSION_REFERENCE_SET = "999000021000001108";

    private static final String ACCEPTABILITY_PREFERRED = "900000000000549004";
    private static final String ACCEPTABILITY_ACCEPTED = "900000000000548007";

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
     * 1. Download the latest Snomed or DM+D release from TRUD (if using the latest delta extract, be sure we've not missed any other deltas, otherwise use the full extract)
     * 2. Then run this utility as:
     *      Main snomed <zip file name and path>
     */
    public static void updateSnomedConceptsAndDescriptions(boolean dmd, String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            if (dmd) {
                LOG.error("Usage: dmd <root of zipped DM+D release>");
            } else {
                LOG.error("Usage: snomed <root of zipped Snomed release>");
            }
            return;
        }

        LOG.info("SNOMED/DM+D Update Starting");

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

        if (dmd) {
            importDmd(file, threadPool);

        } else {
            importSnomed(file, threadPool);
        }


        List<ThreadPoolError> errors = threadPool.waitAndStop();
        handleErrors(errors);

        LOG.info("SNOMED/DM+D Update Complete");
    }

    private static void importDmd(File file, ThreadPool threadPool) throws Exception {

        Map<String, SnomedDescDetails> hmByDescId = new HashMap<>();
        Map<String, List<SnomedDescDetails>> hmByConceptId = new HashMap<>();

        LOG.info("Doing DM+D CT Description->Concept Mappings");
        ZipInputStream zis = ZipHelper.createZipInputStream(file);
        Reader r = ZipHelper.findFile(zis, "SnomedCT_UKDrugRF2_PRODUCTION_.*/Snapshot/Terminology/sct2_Description_Snapshot-en_GB1000001_.*.txt");
        if (r == null) {
            throw new Exception("Failed to find international description file");
        }
        processDescriptionFile(r, threadPool, hmByDescId, hmByConceptId);
        zis.close();

        //we now need to process the UK refset file to work out the preferred description for each concept
        LOG.info("Doing DM+D Refset");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "SnomedCT_UKDrugRF2_PRODUCTION_.*/Snapshot/Refset/Language/der2_cRefset_LanguageSnapshot-en_GB1000001_.*.txt");
        if (r == null) {
            throw new Exception("Failed to find UK refset file");
        }
        processRefSetFile(r, threadPool, hmByDescId, hmByConceptId);
        zis.close();
    }

    private static void importSnomed(File file, ThreadPool threadPool) throws Exception {

        Map<String, SnomedDescDetails> hmByDescId = new HashMap<>();
        Map<String, List<SnomedDescDetails>> hmByConceptId = new HashMap<>();


        //find international concepts file
        LOG.info("Doing International Snomed CT Description->Concept Mappings");
        ZipInputStream zis = ZipHelper.createZipInputStream(file);
        Reader r = ZipHelper.findFile(zis, "SnomedCT_InternationalRF2_PRODUCTION_.*/Snapshot/Terminology/sct2_Description_Snapshot-en_INT_.*.txt");
        if (r == null) {
            throw new Exception("Failed to find international description file");
        }
        processDescriptionFile(r, threadPool, hmByDescId, hmByConceptId);
        zis.close();

        //find UK concepts file
        LOG.info("Doing UK Snomed CT Description->Concept Mappings");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "SnomedCT_UKClinicalRF2_PRODUCTION_.*/Snapshot/Terminology/sct2_Description_Snapshot-en_GB1000000_.*.txt");
        if (r == null) {
            throw new Exception("Failed to find UK description file");
        }
        processDescriptionFile(r, threadPool, hmByDescId, hmByConceptId);
        zis.close();

        //we now need to process the UK refset file to work out the preferred description for each concept
        LOG.info("Doing UK Snomed Refset");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, "SnomedCT_UKClinicalRF2_PRODUCTION_.*/Snapshot/Refset/Language/der2_cRefset_LanguageSnapshot-en_GB1000000_.*.txt");
        if (r == null) {
            throw new Exception("Failed to find UK refset file");
        }
        processRefSetFile(r, threadPool, hmByDescId, hmByConceptId);
        zis.close();
    }

    private static void processDescriptionFile(Reader r, ThreadPool threadPool, Map<String, SnomedDescDetails> hmByDescId, Map<String, List<SnomedDescDetails>> hmByConceptId) throws Exception {

        //save the description -> concept mappings as we go along, in batches
        Map<String, String> descriptionBatch = new HashMap<>();
        int descriptionsDone = 0;


        //the concept -> term mappings can only be saved at the end
        /*Map<String, String> conceptTypeCache = new HashMap<>();
        Map<String, String> conceptTermCache = new HashMap<>();*/

        CSVParser parser = new CSVParser(r, CSV_FORMAT);
        int recordNum = 0;
        String lastDescriptionId = null;

        try {
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                recordNum++;
                CSVRecord record = iterator.next();

                String descriptionId = record.get("id");
                String active = record.get("active");
                String conceptId = record.get("conceptId");
                String typeId = record.get("typeId");
                String term = record.get("term");

                if (!typeId.equals(TYPE_FULLY_SPECIFIED_NAME)
                        && !typeId.equals(TYPE_SYNONYM)) {
                    throw new Exception("Unexpected type ID " + typeId + " for concept " + conceptId + " and term ID " + descriptionId);
                }

                //save the description to concept mapping
                descriptionBatch.put(descriptionId, conceptId);
                saveDescriptionMappings(descriptionBatch, threadPool, false);
                descriptionsDone++;
                if (descriptionsDone % 5000 == 0) {
                    LOG.info("Saved " + descriptionsDone + " description->concept mappings");
                }

                if (active.equals("1")) {
                    SnomedDescDetails o = new SnomedDescDetails(term, typeId);

                    hmByDescId.put(descriptionId, o);

                    List<SnomedDescDetails> l = hmByConceptId.get(conceptId);
                    if (l == null) {
                        l = new ArrayList<>();
                        hmByConceptId.put(conceptId, l);
                    }
                    l.add(o);
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
        /*List<SnomedLookup> conceptBatch = new ArrayList<>();
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
        saveConceptMappings(conceptBatch, threadPool, true);*/

        List<ThreadPoolError> errors = threadPool.waitUntilEmpty();
        handleErrors(errors);
    }


    private static void processRefSetFile(Reader r, ThreadPool threadPool, Map<String, SnomedDescDetails> hmByDescId, Map<String, List<SnomedDescDetails>> hmByConceptId) throws Exception {

        //parse the refset file to find the preferred term for each concept
        CSVParser parser = new CSVParser(r, CSV_FORMAT);
        int recordNum = 0;

        try {
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                recordNum++;
                if (recordNum % 5000 == 0) {
                    LOG.info("Processed " + recordNum + " refset records");
                }

                CSVRecord record = iterator.next();

                String active = record.get("active");
                String module = record.get("moduleId");
                String descriptionId = record.get("referencedComponentId");
                String acceptabilityId = record.get("acceptabilityId");

                if (!module.equals(MODULE_UK_CLINICAL_EXTENSION_REFERENCE_SET)
                        && !module.equals(MODULE_UK_EDITION)
                        && !module.equals(MODULE_UK_CLINICAL_EXTENSION)
                        && !module.equals(MODULE_UK_DRUG_EXTENSION)
                        && !module.equals(MODULE_UK_DRUG_EXTENSION_REFERENCE_SET)) {
                    throw new Exception("Unexpected moduleId " + module + " for term ID " + descriptionId);
                }

                if (!acceptabilityId.equals(ACCEPTABILITY_ACCEPTED)
                        && !acceptabilityId.equals(ACCEPTABILITY_PREFERRED)) {
                    throw new Exception("Unexpected acceptabilityId " + acceptabilityId + " for term ID " + descriptionId);
                }

                if (!active.equals("1")) {
                    continue;
                }

                boolean isPreferred = acceptabilityId.equals(ACCEPTABILITY_PREFERRED);

                SnomedDescDetails termDetails = hmByDescId.get(descriptionId);

                //need to handle null as there is at least one case of the refset pointing to a non-active desceiption (desc ID 3311408011)
                if (termDetails != null) {
                    termDetails.setRefsetPreferred(new Boolean(isPreferred));
                }
            }

        } catch (Exception ex) {
            LOG.error("Error on line " + recordNum);
            throw ex;

        } finally {
            parser.close();
        }

        //save concept IDs to DB
        LOG.info("Now going to save preferred concept->term mappings to DB");
        List<SnomedLookup> conceptBatch = new ArrayList<>();
        int conceptsDone = 0;

        for (String conceptId: hmByConceptId.keySet()) {
            List<SnomedDescDetails> refsetRecords = hmByConceptId.get(conceptId);
            if (refsetRecords == null
                    || refsetRecords.isEmpty()) {
                throw new Exception("No descriptions found for concept " + conceptId);
            }

            //sort the concepts into "best" order
            try {
                refsetRecords.sort(((o1, o2) -> o1.compareTo(o2)));
            } catch (Exception ex) {
                LOG.error("Exception comparing records for concept " + conceptId + ":");
                for (SnomedDescDetails s: refsetRecords) {
                    LOG.error("" + s);
                }
                throw ex;
            }

            SnomedDescDetails bestRecord = refsetRecords.get(0);

            SnomedLookup bestTerm = new SnomedLookup();
            bestTerm.setTerm(bestRecord.getDescription());
            bestTerm.setTypeId(bestRecord.getDescriptionTypeId());
            bestTerm.setConceptId(conceptId);

            conceptBatch.add(bestTerm);
            conceptsDone ++;
            saveConceptMappings(conceptBatch, threadPool, false);
            if (conceptsDone % 5000 == 0) {
                LOG.info("Saved " + conceptsDone + " concept->term mappings");
            }
        }

        LOG.info("Saved " + conceptsDone + " concept->term mappings");
        saveConceptMappings(conceptBatch, threadPool, true);

        List<ThreadPoolError> errors = threadPool.waitUntilEmpty();
        handleErrors(errors);
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

                DeadlockHandler h = new DeadlockHandler();
                while (true) {
                    try {
                        dal.saveSnomedDescriptionToConceptMappings(mappings);
                        break;

                    } catch (Exception ex) {
                        h.handleError(ex);
                    }
                }

            } catch (Throwable t) {
                LOG.error("", t);
                throw t;
            }

            return null;
        }
    }

    static class SnomedDescDetails {
        private String description;
        private String descriptionTypeId; //Full specified name, synonym, from descriptions file
        private Boolean refsetPreferred; //whether a preferred term or not, from refset file

        public SnomedDescDetails(String description, String descriptionTypeId) {
            this.description = description;
            this.descriptionTypeId = descriptionTypeId;
        }

        public Boolean getRefsetPreferred() {
            return refsetPreferred;
        }

        public void setRefsetPreferred(Boolean refsetPreferred) {
            this.refsetPreferred = refsetPreferred;
        }

        public String getDescription() {
            return description;
        }

        public String getDescriptionTypeId() {
            return descriptionTypeId;
        }

        @Override
        public String toString() {
            return "Desc [" + description + "] type " + descriptionTypeId + " refsetPreferred " + refsetPreferred;
        }

        /**
         * order of ranking (best to worst)
         * 1. REFSET full Specified Name
         * 2. REFSET preferred
         * 3. REFSET acceptable
         * 4. non-refset full Specified Name
         * 5. non-refset preferred
         * 6. non-refset acceptable
         */
        public int compareTo(SnomedDescDetails other) {

            //always prefer descriptions found in the refset
            if (refsetPreferred != null
                    && other.getRefsetPreferred() == null) {
                return -1;

            } else if (refsetPreferred == null
                    && other.getRefsetPreferred() != null) {
                return 1;

            } else {

                //prefer fully specified names over synonyms
                if (descriptionTypeId.equals(TYPE_FULLY_SPECIFIED_NAME)
                        && !other.getDescriptionTypeId().equals(TYPE_FULLY_SPECIFIED_NAME)) {
                    return -1;

                } else if (!descriptionTypeId.equals(TYPE_FULLY_SPECIFIED_NAME)
                        && other.getDescriptionTypeId().equals(TYPE_FULLY_SPECIFIED_NAME)) {
                    return 1;

                } else {
                    //then we prefer "preferred" terms over "acceptable" ones (handling null refset status in both cases)
                    if (refsetPreferred != null
                            && refsetPreferred.booleanValue()
                            && other.getRefsetPreferred() != null
                            && !other.getRefsetPreferred().booleanValue()) {
                        return -1;

                    } else if (refsetPreferred != null
                            && !refsetPreferred.booleanValue()
                            && other.getRefsetPreferred() != null
                            && other.getRefsetPreferred().booleanValue()) {
                        return 1;

                    } else {
                        return 0;
                    }
                }

            }


        }
    }
}
