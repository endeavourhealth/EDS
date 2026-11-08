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

public class OnsCcgHelper {
    private static final Logger LOG = LoggerFactory.getLogger(OnsCcgHelper.class);

    //cols changed in May 2026 and no longer have the weird character prefixing the first column header
    private static final String COL_UNKNOWN = "ICB26CD"; //No longer has the weird character (SD-1211)
    private static final String COL_CODE = "ICB26CDH";
    private static final String COL_NAME = "ICB26NM";

    //cols change in new file April 2023 for ICBs
    /*private static final String COL_UNKNOWN = "\uFEFFICB23CD"; //like most ONS files, has a weird character at the start
    private static final String COL_CODE = "ICB23CDH";
    private static final String COL_NAME = "ICB23NM";*/
    //cols changed July 2022
    /*private static final String COL_UNKNOWN = "\uFEFFLOC22CD"; //like most ONS files, has a weird character at the start
    private static final String COL_CODE = "LOC22CDH";
    private static final String COL_NAME = "LOC22NM";*/
    //cols changed May 2021
    /*private static final String COL_UNKNOWN = "\uFEFFCCG21CD"; //like most ONS files, has a weird character at the start
    private static final String COL_CODE = "CCG21CDH";
    private static final String COL_NAME = "CCG21NM"; */
    //cols changed May 2020
    /*private static final String COL_UNKNOWN = "\uFEFFCCG20CD"; //like most ONS files, has a weird character at the start
    private static final String COL_CODE = "CCG20CDH";
    private static final String COL_NAME = "CCG20NM";*/
    //cols changed Apr 2019
    /*private static final String COL_UNKNOWN = "\uFEFFCCG19CD"; //like most ONS files, has a weird character at the start
    private static final String COL_CODE = "CCG19CDH";
    private static final String COL_NAME = "CCG19NM";*/
    /*private static final String COL_UNKNOWN = "\uFEFFCCG18CD"; //not sure what this code is, but we don't use it
    private static final String COL_CODE = "CCG18CDH";
    private static final String COL_NAME = "CCG18NM";
    private static final String COL_NAME_WELSH = "CCG18NMW";*/

    public static void processFile(Reader r) throws Exception {

        Map<String, String> map = readFile(r);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String code: map.keySet()) {
            String name = map.get(code);

            referenceUpdaterDal.updateCcgMap(code, name);

            done ++;
            if (done % 50 == 0) {
                LOG.info("Done " + done + " ICB mappings (out of approx 36)");
            }
        }
    }


    private static Map<String, String> readFile(Reader r) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is Comma delimied
        //changed to CSV in May 2018
        CSVFormat format = CSVFormat.DEFAULT;
        //CSVFormat format = CSVFormat1.TDF;

        CSVParser parser = null;
        try {
            parser = new CSVParser(r, format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            //cols changed again in new file Apr 2023
            //String[] expectedHeaders = new String[]{COL_UNKNOWN, COL_CODE, COL_NAME, COL_NAME_WELSH};
            String[] expectedHeaders = new String[]{COL_UNKNOWN, COL_CODE, COL_NAME};
            CsvHelper.validateCsvHeaders(parser, "ICB File", expectedHeaders);

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

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Documents\Names and Codes\CCG names and codes UK as at 04_18.csv
        String root = args[1];
        return Main.findFile("csv", "CCG.*UK.*", root, "Documents", "Names and Codes");
    }*/
}
