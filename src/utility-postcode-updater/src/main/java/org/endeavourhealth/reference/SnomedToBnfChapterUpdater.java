package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.SnomedToBnfChapterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SnomedToBnfChapterUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedToBnfChapterUpdater.class);

    /**
     * updates the snomed_to_bnf_chapter_lookup table in the reference DB from NHS BSA data
     *
     * Usage
     * =================================================================================
     * 1. Download the data file from https://www.nhsbsa.nhs.uk/prescription-data/understanding-our-data/bnf-snomed-mapping
     * 2. Open the zip file (e.g. June 2018 Snomed mapping.zip).
     * 3. Convert the .xlsx file (normally named same as zip) to .csv, retaining just the columns
     *    "VMPP / AMPP SNOMED Code" and "BNF Code" (in that order), dropping the leading single
     *    quote (') from both columns, but keeping the full contents of the cells i.e. put them
     *    into text format, so that Excel doesn't change them to scientific (exponential) notation.
     * 4. Then run this utility as:
     *    Main snomedToBnfChapter <filename.csv> (e.g. June_2018_Snomed_To_BNF_Chapter_Mapping.csv)
     */

    public static void updateSnomedToBnfChapterLookup(String[] args) throws Exception {
        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: snomedToBnfChapter <filename.csv>");
        }

        LOG.info("Snomed To BNF Chapter Update Starting.");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist.");
        }

        saveSnomedToBnfLookups(file);
        LOG.info("Finished Snomed To BNF Chapter Import.");
    }

    private static void saveSnomedToBnfLookups(File file) throws Exception {
        Map<String, String> codeMap = readSnomedToBnfChapterRecords(file);
        int done = 0;

        SnomedToBnfChapterDalI dal = DalProvider.factorySnomedToBnfChapter();

        for (String snomedCode: codeMap.keySet()) {
            String bnfChapterCode = codeMap.get(snomedCode);

            dal.updateSnomedToBnfChapterLookup(snomedCode, bnfChapterCode);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " Snomed To BNF Chapter Lookups (out of approx 140k).");
            }
        }

        LOG.info("Done " + done + " Snomed To BNF Chapter Lookups");
    }

    /**
     * the .csv file should be two columns delimited with a comma, with column headers.
     */
    private static Map<String, String> readSnomedToBnfChapterRecords(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        // CSVFormat format = CSVFormat.DEFAULT;

        CSVFormat format = CSVFormat.newFormat(',')
                .withFirstRecordAsHeader()
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true);

        CSVParser parser = null;

        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String snomedCode = record.get(0);
                String bnfChapterCode = record.get(1);
                map.put(snomedCode, bnfChapterCode);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }
}
