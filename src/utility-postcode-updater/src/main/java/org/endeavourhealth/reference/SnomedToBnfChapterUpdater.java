package org.endeavourhealth.reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SnomedToBnfChapterUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(SnomedToBnfChapterUpdater.class);

    /**
     * updates the snomed_to_bnf_chapter_lookup table in the reference DB from NHS BSA data
     *
     * Usage
     * =================================================================================
     * 1. Download the data file from https://www.nhsbsa.nhs.uk/prescription-data/understanding-our-data/bnf-snomed-mapping
     * 2. Open the zip file (e.g. "June 2018 Snomed mapping.zip")
     * 3. Convert the .xlsx file (normally named same as zip) to .csv, retaining just the columns
     *    "VMPP / AMPP SNOMED Code" and "BNF Code" (in that order), dropping the leading single
     *    quote (') from both columns, but keeping the full contents of the cells i.e. put them
     *    into text format, so that Excel does not change them to scientific (exponential) notation
     * 4. Then run this utility as:
     *    Main snomedToBnfChapter <filename.csv> (e.g. "June 2018 Snomed To BNF Chapter Mapping.csv")
     */

    public static void updateSnomedToBnfChapterLookup(String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: snomedToBnfChapter <filename.csv>");
        }

        LOG.info("Snomed To BNF Chapter Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist.");
        }

        // saveSnomedToBnfLookups(file);
        LOG.info("Finished Snomed To BNF Chapter Import.");
    }

}
