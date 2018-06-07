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

public class CcgUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(CcgUpdater.class);

    private static final String COL_UNKNOWN = "\uFEFFCCG18CD"; //not sure what this code is, but we don't use it
    private static final String COL_CODE = "CCG18CDH";
    private static final String COL_NAME = "CCG18NM";
    private static final String COL_NAME_WELSH = "CCG18NMW";


    /**
     * utility to update the ccg_lookup table in the reference DB from ONS data
     *
     * Usage
     * =================================================================================
     * 1. See comment in PostcodeUpdater on where to download data from
     * 2. Then extract the archive
     * 3. Locate the "CCG names and codes as ..." TXT file in the Documents\Names and Codes directory,
     * 4. Then run this utility as:
     *      Main ccg <txt file>
     */
    public static void updateCcgs(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: ccg <txt file>");
            return;
        }

        LOG.info("CCG Update Starting");

        File mapFile = new File(args[1]);

        if (!mapFile.exists()) {
            LOG.error("" + mapFile + " doesn't exist");
        }

        LOG.info("Processing CCG map");
        saveMappings(mapFile);
        LOG.info("Finished CCG map");
    }


    private static void saveMappings(File mapFile) throws Exception {

        Map<String, String> map = readFile(mapFile);
        int done = 0;

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();

        for (String code: map.keySet()) {
            String name = map.get(code);

            referenceUpdaterDal.updateCcgMap(code, name);

            done ++;
            if (done % 50 == 0) {
                LOG.info("Done " + done + " CCG mappings (out of approx 200)");
            }
        }
    }


    private static Map<String, String> readFile(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        //this map file is TAB delimied
        //changed to CSV in May 2018
        CSVFormat format = CSVFormat.DEFAULT;
        //CSVFormat format = CSVFormat1.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            //validate the headers are what we expect
            String[] expectedHeaders = new String[]{COL_UNKNOWN, COL_CODE, COL_NAME, COL_NAME_WELSH};
            CsvHelper.validateCsvHeaders(parser, src.getAbsolutePath(), expectedHeaders);

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


}
