package org.endeavourhealth.postcodeupdater;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeHelper;
import org.endeavourhealth.core.rdbms.reference.PostcodeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String LSOA_MAP_CODE = "LSOA11CD";
    private static final String LSOA_MAP_NAME = "LSOA11NM";

    private static final String MSOA_MAP_CODE = "MSOA11CD";
    private static final String MSOA_MAP_NAME = "MSOA11NM";

    private static final String TOWNSEND_MAP_WARD_CODE = "Ward-Code";
    private static final String TOWNSEND_MAP_WARD_NAME = "Ward-Name";
    private static final String TOWNSEND_MAP_SCORE = "Townsend01";
    private static final String TOWNSEND_MAP_QUINTILES = "Quintiles";

    private static final String POSTCODE_8_CHAR_FIXED = "PCD2";
    private static final String POSTCODE_SINGLE_SPACE = "PCDS";
    private static final String POSTCODE_DATE_ADDED = "DOINTR";
    private static final String POSTCODE_DATE_REMOVED = "DOTERM";
    private static final String POSTCODE_100M_EATING = "OSEAST100M";
    private static final String POSTCODE_100M_NORTHING = "OSNRTH100M";
    private static final String POSTCODE_COUNTY_CODE = "OSCTY";
    private static final String POSTCODE_LA_ORGANISATION = "ODSLAUA";
    private static final String POSTCODE_LA_DISTRICT = "OSLAUA";
    private static final String POSTCODE_WARD = "OSWARD";
    private static final String POSTCODE_USER_TYPE = "USERTYPE";
    private static final String POSTCODE_GRID_REFERENCE_QUALITY = "OSGRDIND";
    private static final String POSTCODE_COUNTRY = "CTRY";
    private static final String POSTCODE_FORMER_SHA_CODE = "OSHLTHAU";
    private static final String POSTCODE_REGION_CODE = "GOR";
    private static final String POSTCODE_FORMER_HA_CODE = "OLDHA";
    private static final String POSTCODE_COMMISSIONING_REGION_CODE = "NHSCR";
    private static final String POSTCODE_CCG_CODE = "CCG";
    private static final String POSTCODE_CENSUS_ENUMERATION_DISTRICT = "PSED";
    private static final String POSTCODE_CENSUS_ENUMERATION_DISTRICT_2 = "CENED";
    private static final String POSTCODE_ENUMERATION_DISTRICT_QUALITY_INDICATOR = "EDIND";
    private static final String POSTCODE_1998_WARD = "WARD98";
    private static final String POSTCODE_2001_CENSUS_OUTPUT_AREA = "OA01";
    private static final String POSTCODE_NHS_REGION_GEOGRAPHY = "NHSRG";
    private static final String POSTCODE_FORMER_PAN_SHA = "HRO";
    private static final String POSTCODE_2001_CENSUS_LSOA = "LSOA01";
    private static final String POSTCODE_2001_URBAN_RURAL_INDICATOR = "UR01IND";
    private static final String POSTCODE_2001_CENSUS_MSOA = "MSOA01";
    private static final String POSTCODE_FORMER_CANCER_NETWORK = "CANNET";
    private static final String POSTCODE_STRATEGIC_CLINICAL_NETWORK = "SCN";
    private static final String POSTCODE_FIRST_WAVE_SHA = "OSHAPREV";
    private static final String POSTCODE_FIRST_WAVE_PCT = "OLDPCT";
    private static final String POSTCODE_OLD_IT_CLUSTER = "OLDHRO";
    private static final String POSTCODE_PARLIMENTARY_CONSTITUENCY = "PCON";
    private static final String POSTCODE_CANCER_REGISTRY = "CANREG";
    private static final String POSTCODE_SECOND_WAVE_PCT = "PCT";
    private static final String POSTCODE_1M_EASTING = "OSEAST1M";
    private static final String POSTCODE_1M_NORTHING = "OSNRTH1M";
    private static final String POSTCODE_2011_CENSUS_OUTPUT_AREA = "OA11";
    private static final String POSTCODE_2011_CENSUS_LSOA = "LSOA11";
    private static final String POSTCODE_2011_CENSUS_MSOA = "MSOA11";

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
     * 4. Locate the 2011 LSOA names and codes TXT file in the Documents\Names and Codes folder,
     * 5. Locate the 2011 MSOA names and codes TXT file in the Documents\Names and Codes folder,
     * 6. Download the 2001 Townsend deprivation scores CSV file
     * https://census.ukdataservice.ac.uk/get-data/related/deprivation
     * 7. Then run this utility passing in those four files as parameters
     *
     * Parameters
     * =================================================================================
     *
     * 1. the Config name that gives the location of the reference database
     * 2. the raw postcode data file (the big CSV file)
     * 3. the LSOA name and code map TXT file
     * 4. the MSOA name and code map TXT file
     * 5. the Townsend score CSV file
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 5) {
            LOG.error("Incorrect number of parameters. Check usage description in class commment.");
            return;
        }

        String configName = args[0];
        ConfigManager.Initialize(configName);

        try {


            LOG.info("Postcode Reference Update Starting");

            File postcodeFile = new File(args[1]);
            File lsoaMapFile = new File(args[2]);
            File msoaMapFile = new File(args[3]);
            File townsendMapFile = new File(args[4]);

            if (!postcodeFile.exists()) {
                LOG.error("" + postcodeFile + " doesn't exist");
            }
            if (!lsoaMapFile.exists()) {
                LOG.error("" + lsoaMapFile + " doesn't exist");
            }
            if (!msoaMapFile.exists()) {
                LOG.error("" + msoaMapFile + " doesn't exist");
            }
            if (!townsendMapFile.exists()) {
                LOG.error("" + townsendMapFile + " doesn't exist");
            }

            //the main file is huge (800MB+) but the others are comparatively tiny, so process both of them,
            //to build up a map of data that we can refer to when we process the large file
            LOG.info("Reading LSOA map");
            Map<String, String> lsoaMap = readLsoaMap(lsoaMapFile);
            LOG.info("Finished reading LSOA map");

            LOG.info("Reading MSOA map");
            Map<String, String> msoaMap = readMsoaMap(msoaMapFile);
            LOG.info("Finished reading MSOA map");

            LOG.info("Reading Townsend map");
            Map<String, BigDecimal> townsendMap = readTownsendMap(townsendMapFile);
            LOG.info("Finished reading Townsend map");

            //now we've got our two small maps ready, start processing the bulk of the data, which will update the DB
            LOG.info("Processing Postcode file");
            readPostcodeFile(postcodeFile, lsoaMap, msoaMap, townsendMap);
            LOG.info("Postcode Reference Update Complete");

        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    private static void readPostcodeFile(File postcodeFile, Map<String, String> lsoaMap, Map<String, String> msoaMap, Map<String, BigDecimal> townsendMap) throws Exception {

        ThreadPool threadPool = new ThreadPool(5, 1000);

        CSVFormat format = CSVFormat.DEFAULT;

        int rowsDone = 0;
        CSVParser parser = null;
        try {
            //the postcode CSV file doesn't contain headers, so we must just pass in the headers we know should be there
            parser = CSVParser.parse(postcodeFile, Charset.defaultCharset(), format.withHeader(getPostcodeHeadings()));
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String postcode = record.get(POSTCODE_SINGLE_SPACE);
                String lsoaCode = record.get(POSTCODE_2011_CENSUS_LSOA);
                String msoaCode = record.get(POSTCODE_2011_CENSUS_MSOA);
                String ward = record.get(POSTCODE_WARD);
                String ward1998 = record.get(POSTCODE_1998_WARD);
                String ccgCode = record.get(POSTCODE_CCG_CODE);

                String lsoaName = lsoaMap.get(lsoaCode);
                String msoaName = msoaMap.get(msoaCode);
                BigDecimal townsendScore = townsendMap.get(ward1998);

                //always make sure this is uppercase
                postcode = postcode.toUpperCase();

                String postcodeNoSpace = postcode.replace(" ", "");

                PostcodeReference postcodeReference = PostcodeHelper.getPostcodeReference(postcode);
                if (postcodeReference == null) {
                    postcodeReference = new PostcodeReference();
                    postcodeReference.setPostcodeNoSpace(postcodeNoSpace);
                }

                postcodeReference.setPostcode(postcode);
                postcodeReference.setCcg(ccgCode);
                postcodeReference.setLsoaCode(lsoaCode);
                postcodeReference.setLsoaName(lsoaName);
                postcodeReference.setMsoaCode(msoaCode);
                postcodeReference.setMsoaName(msoaName);
                postcodeReference.setWard(ward);
                postcodeReference.setWard1998(ward1998);
                postcodeReference.setTownsendScore(townsendScore);

                //bump saving into a thread pool for speed
                List<ThreadPoolError> errors = threadPool.submit(new SavePostcodeCallable(postcodeReference));
                handleErrors(errors);
                //PostcodeHelper.save(postcodeReference);

                rowsDone ++;
                if (rowsDone % 5000 == 0) {
                    LOG.info("Done " + rowsDone + " postcodes (of approx 2.6M)");
                }
            }

            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);

            LOG.info("Finshed at " + rowsDone + " postcodes");

        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        Exception exception = first.getException();
        throw exception;
    }

    private static Map<String, BigDecimal> readTownsendMap(File townsendMapFile) throws Exception {
        Map<String, BigDecimal> map = new HashMap<>();

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
                if (!Strings.isNullOrEmpty(score)) {
                    map.put(ward, new BigDecimal(score));
                }

            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }

    private static Map<String, String> readLsoaMap(File lsoaMapFile) throws Exception {
        return readerLsoaOrMsoaMapFile(lsoaMapFile, LSOA_MAP_CODE, LSOA_MAP_NAME);
    }

    private static Map<String, String> readMsoaMap(File msoaMapFile) throws Exception {
        return readerLsoaOrMsoaMapFile(msoaMapFile, MSOA_MAP_CODE, MSOA_MAP_NAME);
    }

    private static Map<String, String> readerLsoaOrMsoaMapFile(File src, String codeCol, String nameCol) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{codeCol, nameCol};
            CsvHelper.validateCsvHeaders(parser, src, expectedHeaders);

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String code = record.get(codeCol);
                String name = record.get(nameCol);
                map.put(code, name);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }


    public static String[] getPostcodeHeadings() {
        return new String[]{
            POSTCODE_8_CHAR_FIXED,
            POSTCODE_SINGLE_SPACE,
            POSTCODE_DATE_ADDED,
            POSTCODE_DATE_REMOVED,
            POSTCODE_100M_EATING,
            POSTCODE_100M_NORTHING,
            POSTCODE_COUNTY_CODE,
            POSTCODE_LA_ORGANISATION,
            POSTCODE_LA_DISTRICT,
            POSTCODE_WARD,
            POSTCODE_USER_TYPE,
            POSTCODE_GRID_REFERENCE_QUALITY,
            POSTCODE_COUNTRY,
            POSTCODE_FORMER_SHA_CODE,
            POSTCODE_REGION_CODE,
            POSTCODE_FORMER_HA_CODE,
            POSTCODE_COMMISSIONING_REGION_CODE,
            POSTCODE_CCG_CODE,
            POSTCODE_CENSUS_ENUMERATION_DISTRICT,
            POSTCODE_CENSUS_ENUMERATION_DISTRICT_2,
            POSTCODE_ENUMERATION_DISTRICT_QUALITY_INDICATOR,
            POSTCODE_1998_WARD,
            POSTCODE_2001_CENSUS_OUTPUT_AREA,
            POSTCODE_NHS_REGION_GEOGRAPHY,
            POSTCODE_FORMER_PAN_SHA,
            POSTCODE_2001_CENSUS_LSOA,
            POSTCODE_2001_URBAN_RURAL_INDICATOR,
            POSTCODE_2001_CENSUS_MSOA,
            POSTCODE_FORMER_CANCER_NETWORK,
            POSTCODE_STRATEGIC_CLINICAL_NETWORK,
            POSTCODE_FIRST_WAVE_SHA,
            POSTCODE_FIRST_WAVE_PCT,
            POSTCODE_OLD_IT_CLUSTER,
            POSTCODE_PARLIMENTARY_CONSTITUENCY,
            POSTCODE_CANCER_REGISTRY,
            POSTCODE_SECOND_WAVE_PCT,
            POSTCODE_1M_EASTING,
            POSTCODE_1M_NORTHING,
            POSTCODE_2011_CENSUS_OUTPUT_AREA,
            POSTCODE_2011_CENSUS_LSOA,
            POSTCODE_2011_CENSUS_MSOA,
        };
    }

    static class SavePostcodeCallable implements Callable {

        private PostcodeReference postcodeReference = null;

        public SavePostcodeCallable(PostcodeReference postcodeReference) {
            this.postcodeReference = postcodeReference;
        }

        @Override
        public Object call() throws Exception {
            PostcodeHelper.save(postcodeReference);
            return null;
        }
    }
}
