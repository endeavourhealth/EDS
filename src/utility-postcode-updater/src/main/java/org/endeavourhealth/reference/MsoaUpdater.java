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

    private static final String MSOA_MAP_CODE = "MSOA11CD";
    private static final String MSOA_MAP_NAME = "MSOA11NM";

    /**
     * utility to update the msoa_lookup table in the reference DB from ONS data
     *
     * Usage
     * =================================================================================
     * 1. Download the "NHS Postcode Directory UK Full" dataset from the ONS
     * http://ons.maps.arcgis.com/home/item.html?id=dc23a64fa2e34e1289901b27d91c335b
     * 2. Then extract the archive
     * 3. Locate the 2011 MSOA names and codes TXT file in the Documents\Names and Codes folder,
     * 4. Then run this utility as:
     *      Main msoa <msoa txt file>
     */
    public static void updateMsoas(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: msoa <msoa txt file>");
            return;
        }

        LOG.info("MSOA Update Starting");

        File msoaMapFile = new File(args[1]);

        if (!msoaMapFile.exists()) {
            LOG.error("" + msoaMapFile + " doesn't exist");
        }

        LOG.info("Processing MSOA map");
        saveMsoaMappings(msoaMapFile);
        LOG.info("Finished MSOA map");
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
        CSVFormat format = CSVFormat.TDF;

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


}
