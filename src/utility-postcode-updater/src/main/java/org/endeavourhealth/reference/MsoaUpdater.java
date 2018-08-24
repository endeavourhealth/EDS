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

public class MsoaUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(MsoaUpdater.class);

    private static final String MSOA_MAP_CODE = "\uFEFFMSOA11CD"; //note the weird leading char in the raw file
    private static final String MSOA_MAP_NAME = "MSOA11NM";

    public static void updateMsoas(File msoaMapFile) throws Exception {
        LOG.info("Processing MSOA map from " + msoaMapFile);
        saveMsoaMappings(msoaMapFile);
        LOG.info("Finished MSOA map from " + msoaMapFile);
    }


    private static void saveMsoaMappings(File msoaMapFile) throws Exception {

        Map<String, String> msoaMap = readFile(msoaMapFile);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String msoaCode: msoaMap.keySet()) {
            String msoaName = msoaMap.get(msoaCode);

            referenceUpdaterDal.updateMosaMap(msoaCode, msoaName);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " MSOA mappings (out of approx 7K)");
            }
        }
    }


    private static Map<String, String> readFile(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        //comma delimited since May 2018
        CSVFormat format = CSVFormat.DEFAULT;
        //CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{MSOA_MAP_CODE, MSOA_MAP_NAME};
            CsvHelper.validateCsvHeaders(parser, src.getAbsolutePath(), expectedHeaders);

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                String code = record.get(MSOA_MAP_CODE);
                String name = record.get(MSOA_MAP_NAME);
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

        //C:\SFTPData\postcodes\NHSPD_MAY_2018_UK_FULL\Documents\Names and Codes\MSOA (2011) names and codes UK as at 12_12.csv
        String root = args[1];
        return Main.findFile("csv", "MSOA.*UK.*", root, "Documents", "Names and Codes");
    }
}
