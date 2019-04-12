package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.models.EhccDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EhccUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(EhccUpdater.class);

    /**
     * updates the eng_hosp_cons_curr_lookup table in the reference DB from TRUD data
     * <p>
     * Usage
     * =================================================================================
     * 1. Download the Organisation Reference Data files from https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/5/subpack/242/releases
     * 2. Open the zip file (e.g. org_refdata_1.0.0_20190228000001.zip)
     * 3. Locate the "econcur.csv" file (follow ocsissue/data/econcur.zip)
     * 4. Then run this utility as:
     * Main ehcc <econcur.csv>
     */

    public static void updateEhccLookup(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: ehcc <econcur.csv>");
            return;
        }

        LOG.info("EHCC Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
        }

        saveEhccLookups(file);
        LOG.info("Finished EHCC Import");
    }


    private static void saveEhccLookups(File file) throws Exception {

        List<String[]> ehccList = readEhccRecords(file);
        int done = 0;

        EhccDalI dal = DalProvider.factoryEhccDal();

        for (String[] ehcc : ehccList) {

            // String procedureName = codeMap.get(procedureCode);

            // dal.updateOpcs4Lookup(procedureCode, procedureName);

            done++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " EHCC lookups (out of approx 50k)");
            }
        }

        LOG.info("Done " + done + " OPCS4 lookups");
    }

    /**
     * the file is simply two columns delimited with a tab. No column headers.
     */
    private static List<String[]> readEhccRecords(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String code = record.get(0);
                String name = record.get(1);
                map.put(code, name);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        // return map;
        return null;
    }
}