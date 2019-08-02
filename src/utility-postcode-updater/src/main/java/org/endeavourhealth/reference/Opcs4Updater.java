package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.Opcs4DalI;
import org.endeavourhealth.reference.helpers.OnsLsoaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Opcs4Updater {
    private static final Logger LOG = LoggerFactory.getLogger(OnsLsoaHelper.class);

    /**
     * updates the opcs4_lookup table in the reference DB from TRUD data
     *
     * Usage
     * =================================================================================
     * 1. Download the OPCS4 data files from https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/10/subpack/119/releases
     * 2. Open the zip file
     * 3. Locate the "codes and titles file" e.g. OPCS48 CodesAndTitles Nov 2016 V1.0
     * 4. Then run this utility as:
     *      Main opcs4 <names and titles file.txt>
     *
     * Release on AWS Live is 1 November 2016
     */
    public static void updateOpcs4Lookup(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: opcs4 <opcs4 txt file>");
            return;
        }

        LOG.info("OPCS4 Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
        }

        saveOpcs4Lookups(file);
        LOG.info("Finished OPCS4 Import");
    }

    private static void saveOpcs4Lookups(File file) throws Exception {

        Map<String, String> codeMap = readOpcs4Records(file);
        int done = 0;

        Opcs4DalI dal = DalProvider.factoryOpcs4Dal();

        for (String procedureCode: codeMap.keySet()) {
            String procedureName = codeMap.get(procedureCode);

            dal.updateOpcs4Lookup(procedureCode, procedureName);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " OPCS4 lookups (out of approx 10k)");
            }
        }

        LOG.info("Done " + done + " OPCS4 lookups");
    }

    /**
     * the file is simply two columns delimited with a tab. No column headers.
     */
    private static Map<String, String> readOpcs4Records(File src) throws Exception {
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

        return map;
    }
}
