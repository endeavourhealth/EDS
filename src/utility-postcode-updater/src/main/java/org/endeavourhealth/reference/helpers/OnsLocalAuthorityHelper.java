package org.endeavourhealth.reference.helpers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceUpdaterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OnsLocalAuthorityHelper {
    private static final Logger LOG = LoggerFactory.getLogger(OnsLocalAuthorityHelper.class);

    //cols changes May 2020
    private static final String COL_CODE = "\uFEFFLAD20CD";
    private static final String COL_NAME = "LAD20NM";
    private static final String COL_NAME_WELSH = "LAD20NMW";
    //cols changes Apr 2019
    /*private static final String COL_CODE = "\uFEFFLAD19CD";
    private static final String COL_NAME = "LAD19NM";
    private static final String COL_NAME_WELSH = "LAD19NMW";*/
    /*private static final String COL_CODE = "\uFEFFLAD18CD";
    private static final String COL_NAME = "LAD18NM";
    private static final String COL_NAME_WELSH = "LAD18NMW";*/

    public static void processFile(Reader r) throws Exception {

        Map<String, String> map = readFile(r);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String code: map.keySet()) {
            String name = map.get(code);

            referenceUpdaterDal.updateLocalAuthorityMap(code, name);

            done ++;
            if (done % 50 == 0) {
                LOG.info("Done " + done + " Local Authority mappings (out of approx 400)");
            }
        }
    }


    private static Map<String, String> readFile(Reader r) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        //changed to CSV in May 2018
        //CSVFormat format = CSVFormat.TDF;
        CSVFormat format = CSVFormat.DEFAULT;

        CSVParser parser = null;
        try {
            parser = new CSVParser(r, format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{COL_CODE, COL_NAME, COL_NAME_WELSH};
            CsvHelper.validateCsvHeaders(parser, "Local Authority file", expectedHeaders);

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String code = record.get(COL_CODE);
                String name = record.get(COL_NAME);
                map.put(code, name);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }

    /*public static File findFile(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Incorrect number of parameters, expecting 2");
        }

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Documents\Names and Codes\LA_UA names and codes UK as at 12_18.csv
        String root = args[1];
        return Main.findFile("csv", "LA_UA.*UK.*", root, "Documents", "Names and Codes");
    }*/
}
