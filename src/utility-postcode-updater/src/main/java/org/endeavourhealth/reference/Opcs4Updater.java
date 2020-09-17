package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.Opcs4DalI;
import org.endeavourhealth.reference.helpers.OnsLsoaHelper;
import org.endeavourhealth.reference.helpers.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class Opcs4Updater {
    private static final Logger LOG = LoggerFactory.getLogger(OnsLsoaHelper.class);

    /**
     * updates the opcs4_lookup table in the reference DB from TRUD data
     *
     * Usage
     * =================================================================================
     * 1. Download the OPCS4 data files from https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/10/subpack/119/releases
     * 4. Then run this utility as:
     *      Main opcs4 <zip file path>
     */
    public static void updateOpcs4Lookup(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: opcs4 <path of zip file>");
            return;
        }

        LOG.info("OPCS4 Update Starting");

        File file = new File(args[1]);
        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
        }

        if (!ZipHelper.isZip(file)) {
            LOG.error("" + file + " isn't a zip file");
            return;
        }

        LOG.info("Looking for file...");
        ZipInputStream zis = ZipHelper.createZipInputStream(file);
        Reader r = ZipHelper.findFile(zis, ".*CodesAndTitles.*txt");
        if (r == null) {
            LOG.error("Failed to find CodesAndTitles file");
            return;
        }
        saveOpcs4Lookups(r);
        zis.close();

        LOG.info("Finished OPCS4 Import");
    }

    private static void saveOpcs4Lookups(Reader reader) throws Exception {

        Map<String, String> codeMap = readOpcs4Records(reader);
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
    private static Map<String, String> readOpcs4Records(Reader src) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = new CSVParser(src, format);
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
