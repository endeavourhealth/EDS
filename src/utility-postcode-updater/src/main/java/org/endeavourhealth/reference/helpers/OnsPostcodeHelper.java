package org.endeavourhealth.reference.helpers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceUpdaterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class OnsPostcodeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(OnsPostcodeHelper.class);


    private static final String POSTCODE_8_CHAR_FIXED = "PCD2";
    private static final String POSTCODE_SINGLE_SPACE = "PCDS";
    private static final String POSTCODE_DATE_ADDED = "DOINTR";
    private static final String POSTCODE_DATE_REMOVED = "DOTERM";
    private static final String POSTCODE_100M_EASTING = "OSEAST100M";
    private static final String POSTCODE_100M_NORTHING = "OSNRTH100M";
    private static final String POSTCODE_COUNTY_CODE = "OSCTY";
    private static final String POSTCODE_LA_ORGANISATION = "ODSLAUA";
    private static final String POSTCODE_LA_DISTRICT = "OSLAUA"; //Local authority district (LAD)/unitary authority (UA)/ metropolitan district (MD)/ London borough (LB)/ council area (CA)/district council area (DCA)
    private static final String POSTCODE_WARD = "OSWARD";
    private static final String POSTCODE_USER_TYPE = "USERTYPE";
    private static final String POSTCODE_GRID_REFERENCE_QUALITY = "OSGRDIND";
    private static final String POSTCODE_COUNTRY = "CTRY";
    private static final String POSTCODE_FORMER_SHA_CODE = "OSHLTHAU";
    private static final String POSTCODE_REGION_CODE = "RGN";
    private static final String POSTCODE_FORMER_HA_CODE = "OLDHA";
    private static final String POSTCODE_COMMISSIONING_REGION_CODE = "NHSER";
    private static final String POSTCODE_CCG_CODE = "CCG";
    private static final String POSTCODE_CENSUS_ENUMERATION_DISTRICT = "PSED";
    private static final String POSTCODE_CENSUS_ENUMERATION_DISTRICT_2 = "CENED";
    private static final String POSTCODE_ENUMERATION_DISTRICT_QUALITY_INDICATOR = "EDIND";
    private static final String POSTCODE_1998_WARD = "WARD98";
    private static final String POSTCODE_2001_CENSUS_OUTPUT_AREA = "OA01";
    private static final String POSTCODE_NHS_REGION_GEOGRAPHY = "NHSRLO";
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
    private static final String POSTCODE_CANCER_ALLIANCE_VANGUARD = "CALNCV";
    private static final String POSTCODE_STP_CODE = "STP";

    //private static void readPostcodeFile(File postcodeFile, Map<String, BigDecimal> townsendMap) throws Exception {
    public static void processFile(Reader r) throws Exception {

        ThreadPool threadPool = new ThreadPool(5, 1000);

        CSVFormat format = CSVFormat.DEFAULT;

        int rowsDone = 0;
        CSVParser parser = null;
        try {
            //the postcode CSV file doesn't contain headers, so we must just pass in the headers we know should be there
            parser = new CSVParser(r, format.withHeader(getPostcodeHeadings()));
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                //bump saving into a thread pool for speed
                //List<ThreadPoolError> errors = threadPool.submit(new SavePostcodeCallable(record, townsendMap));
                List<ThreadPoolError> errors = threadPool.submit(new SavePostcodeCallable(record));
                handleErrors(errors);

                rowsDone ++;
                if (rowsDone % 5000 == 0) {
                    LOG.info("Done " + rowsDone + " postcodes (of approx 2.6M)");
                }
            }

            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);

            LOG.info("Finished at " + rowsDone + " postcodes");

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
        Throwable cause = first.getException();
        //the cause may be an Exception or Error so we need to explicitly
        //cast to the right type to throw it without changing the method signature
        if (cause instanceof Exception) {
            throw (Exception)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        }
    }

    /*private static Map<String, BigDecimal> readTownsendMap(File townsendMapFile) throws Exception {
        Map<String, BigDecimal> map = new ConcurrentHashMap<>();

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
    }*/




    public static String[] getPostcodeHeadings() {
        return new String[]{
                POSTCODE_8_CHAR_FIXED,
                POSTCODE_SINGLE_SPACE,
                POSTCODE_DATE_ADDED,
                POSTCODE_DATE_REMOVED,
                POSTCODE_100M_EASTING,
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
                POSTCODE_CANCER_ALLIANCE_VANGUARD,
                POSTCODE_STP_CODE,
        };
    }

    static class SavePostcodeCallable implements Callable {

        private CSVRecord record = null;
        //private Map<String, BigDecimal> townsendMap = null;

        public SavePostcodeCallable(CSVRecord record) {
            this.record = record;
        }
        /*public SavePostcodeCallable(CSVRecord record, Map<String, BigDecimal> townsendMap) {
            this.record = record;
            this.townsendMap = townsendMap;
        }*/

        @Override
        public Object call() throws Exception {

            String postcode = record.get(POSTCODE_SINGLE_SPACE);
            String lsoaCode = record.get(POSTCODE_2011_CENSUS_LSOA);
            String msoaCode = record.get(POSTCODE_2011_CENSUS_MSOA);
            String ward = record.get(POSTCODE_WARD);
            String ccgCode = record.get(POSTCODE_CCG_CODE);
            String localAuthority = record.get(POSTCODE_LA_DISTRICT);
            String lsoa2001Code = record.get(POSTCODE_2001_CENSUS_LSOA);
            String lsoa2011Code = record.get(POSTCODE_2011_CENSUS_LSOA);
            String msoa2001Code = record.get(POSTCODE_2001_CENSUS_MSOA);
            String msoa2011Code = record.get(POSTCODE_2011_CENSUS_MSOA);

            ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();
            referenceUpdaterDal.updatePostcodeMap(postcode, lsoaCode, msoaCode, ward, ccgCode, localAuthority,
                    lsoa2001Code, lsoa2011Code, msoa2001Code, msoa2011Code);

            return null;
        }
    }


    /*public static File findFile(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Incorrect number of parameters, expecting 2");
        }

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Data\nhg18may.csv
        String root = args[1];
        File dir = new File(root);
        if (!dir.exists()) {
            throw new RuntimeException("" + dir + " does not exist");
        }
        dir = new File(dir, "Data");
        if (!dir.exists()) {
            throw new RuntimeException("" + dir + " does not exist");
        }

        //it's the largest CSV file in this dir
        File ret = null;
        long retSize = -1;

        for (File child: dir.listFiles()) {
            String ext = FilenameUtils.getExtension(child.getName());
            if (!ext.equalsIgnoreCase("csv")) {
                continue;
            }

            if (ret == null
                    || child.length() > retSize) {
                ret = child;
                retSize = child.length();
            }
        }

        if (ret == null) {
            throw new RuntimeException("Failed to find postcode csv file in " + dir);
        }

        return ret;
    }*/
}

