package org.endeavourhealth.reference.helpers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceUpdaterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OnsWardHelper {
    private static final Logger LOG = LoggerFactory.getLogger(OnsWardHelper.class);

    //cols changed May 20202
    private static final String COL_CODE = "\uFEFFWD20CD";
    private static final String COL_NAME = "WD20NM";
    //cols changed Apr 2018
    /*private static final String COL_CODE = "\uFEFFWD19CD";
    private static final String COL_NAME = "WD19NM";*/
    //private static final String COL_NAME_WELSH = "WD19NMW"; //removed in Dec 2019 version
    /*private static final String COL_CODE = "\uFEFFWD18CD";
    private static final String COL_NAME = "WD18NM";
    private static final String COL_NAME_WELSH = "WD18NMW";*/

    public static void processFile(Reader r) throws Exception {

        Map<String, String> map = readFile(r);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String code: map.keySet()) {
            String name = map.get(code);

            referenceUpdaterDal.updateWardMap(code, name);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " Ward mappings (out of approx 9k)");
            }
        }
    }


    private static Map<String, String> readFile(Reader r) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        //changed to CSV in May 2018
        CSVFormat format = CSVFormat.DEFAULT;
        //CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            //in the May 2020 release they had a load of extra empty columns, so need to turn on the option to allow them
            parser = new CSVParser(r, format.withHeader().withAllowMissingColumnNames());
            //parser = new CSVParser(r, format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect

            //because of garbage empty columns, this fails. So just check manually
            Map<String, Integer> headers = parser.getHeaderMap();
            if (!headers.containsKey(COL_CODE)
                    || !headers.containsKey(COL_NAME)) {
                throw new Exception("Missing column header in Ward file with headers [" + headers + "]");
            }
            /*String[] expectedHeaders = new String[]{COL_CODE, COL_NAME};
            CsvHelper.validateCsvHeaders(parser, "Ward File", expectedHeaders);*/

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

   /* public static File findFile(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Incorrect number of parameters, expecting 2");
        }

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Documents\Names and Codes\Ward names and codes UK as at 05_18.csv
        String root = args[1];
        return Main.findFile("csv", "Ward.*UK.*[^9].$", root, "Documents", "Names and Codes");
    }*/
}
