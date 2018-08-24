package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceUpdaterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LsoaUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(LsoaUpdater.class);

    private static final String LSOA_MAP_CODE = "\uFEFFLSOA11CD"; //May 2018 file has a leading weird char
    private static final String LSOA_MAP_NAME = "LSOA11NM";

    public static void updateLsoas(File lsoaMapFile) throws Exception {
        LOG.info("Processing LSOA map from " + lsoaMapFile);
        saveLsoaMappings(lsoaMapFile);
        LOG.info("Finished LSOA map from " + lsoaMapFile);
    }

    private static void saveLsoaMappings(File lsoaMapFile) throws Exception {

        Map<String, String> lsoaMap = readFile(lsoaMapFile);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String lsoaCode: lsoaMap.keySet()) {
            String lsoaName = lsoaMap.get(lsoaCode);

            referenceUpdaterDal.updateLosaMap(lsoaCode, lsoaName);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " LSOA mappings (out of approx 35K)");
            }
        }
    }


    private static Map<String, String> readFile(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        //format has changed in 2018 to comma delimited
        //CSVFormat format = CSVFormat.TDF;
        CSVFormat format = CSVFormat.DEFAULT;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{LSOA_MAP_CODE, LSOA_MAP_NAME};
            CsvHelper.validateCsvHeaders(parser, src.getAbsolutePath(), expectedHeaders);

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

    public static File findFile(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Incorrect number of parameters, expecting 2");
        }

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Documents\Names and Codes\LSOA (2011) names and codes UK as at 12_12.csv
        String root = args[1];
        return Main.findFile("csv", "LSOA.*UK.*", root, "Documents", "Names and Codes");
    }
}
