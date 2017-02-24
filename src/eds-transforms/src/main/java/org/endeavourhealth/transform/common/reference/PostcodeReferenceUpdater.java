package org.endeavourhealth.transform.common.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PostcodeReferenceUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(PostcodeReferenceUpdater.class);

    private static final String LSOA_MAP_CODE = "LSOA04CD";
    private static final String LSOA_MAP_NAME = "LSOA04NM";

    private static final String TOWNSEND_MAP_WARD_CODE = "Ward-Code";
    private static final String TOWNSEND_MAP_WARD_NAME = "Ward-Name";
    private static final String TOWNSEND_MAP_SCORE = "Townsend01";
    private static final String TOWNSEND_MAP_QUINTILES = "Quintiles";

    /**
     * utility to update the PostcodeReference table from ONS data
     *
     * Usage
     * =================================================================================
     *
     * 1. Download the "NHS Postcode Directory UK Full" dataset from the ONS
     * http://ons.maps.arcgis.com/home/item.html?id=dc23a64fa2e34e1289901b27d91c335b
     * 2. Then extract the archive
     * 3. Locate the large (800MB+) CSV file - this is the raw postcode data file
     * 4. Locate the LSOA names and codes TXT file in the Documents\Names and Codes folder,
     * choosing the most recent one, if there are multiple - this is a map of LSOA codes to names
     * 5. Download the 2001 Townsend deprivation scores CSV file
     * https://census.ukdataservice.ac.uk/get-data/related/deprivation
     * 6. Then run this utility passing in those three files as parameters
     *
     * Parameters
     * =================================================================================
     *
     * 1. the raw postcode data file (the big CSV file)
     * 2. the LSOA name and code map TXT file
     * 3. the Townsend score CSV file
     */
    public static void main(String[] args) {

        try {
            if (args.length != 3) {
                LOG.error("Incorrect number of parameters. Check usage description in class commment.");
                return;
            }

            LOG.info("Postcode Reference Update Starting");

            File postcodeFile = new File(args[0]);
            File lsoaMapFile = new File(args[1]);
            File townsendMapFile = new File(args[2]);

            if (!postcodeFile.exists()) {
                LOG.error("" + postcodeFile + " doesn't exist");
            }
            if (!lsoaMapFile.exists()) {
                LOG.error("" + lsoaMapFile + " doesn't exist");
            }
            if (!townsendMapFile.exists()) {
                LOG.error("" + townsendMapFile + " doesn't exist");
            }

            //the main file is huge (800MB+) but the others are comparatively tiny, so process both of them,
            //to build up a map of data that we can refer to when we process the large file
            LOG.info("Reading LSOA map");
            Map<String, String> lsoaMap = readLsoaMap(lsoaMapFile);
            LOG.info("Finished reading LSOA map");

            LOG.info("Reading Townsend map");
            Map<String, String> townsendMap = readTownsendMap(townsendMapFile);
            LOG.info("Finished reading Townsend map");

            //now we've got our two small maps ready, start processing the bulk of the data, which will update the DB
            LOG.info("Processing Postcode file");
            readPostcodeFile(postcodeFile, lsoaMap, townsendMap);
            LOG.info("Postcode Reference Update Complete");

        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    private static void readPostcodeFile(File postcodeFile, Map<String, String> lsoaMap, Map<String, String> townsendMap) {


    }

    private static Map<String, String> readTownsendMap(File townsendMapFile) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.DEFAULT;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(townsendMapFile, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{TOWNSEND_MAP_WARD_CODE, TOWNSEND_MAP_WARD_NAME, TOWNSEND_MAP_SCORE, TOWNSEND_MAP_QUINTILES};
            CsvHelper.validateCsvHeaders(parser, townsendMapFile, expectedHeaders);

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String ward = record.get(TOWNSEND_MAP_WARD_CODE);
                String score = record.get(TOWNSEND_MAP_SCORE);
                map.put(ward, score);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }

    private static Map<String, String> readLsoaMap(File lsoaMapFile) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(lsoaMapFile, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{LSOA_MAP_CODE, LSOA_MAP_NAME};
            CsvHelper.validateCsvHeaders(parser, lsoaMapFile, expectedHeaders);

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String code = record.get(LSOA_MAP_CODE);
                String name = record.get(LSOA_MAP_NAME);
                map.put(code, name);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }
}
