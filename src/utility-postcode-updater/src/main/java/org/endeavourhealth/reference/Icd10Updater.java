package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.Icd10DalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Icd10Updater {
    private static final Logger LOG = LoggerFactory.getLogger(LsoaUpdater.class);

    /**
     * updates the icd10_lookup table in the reference DB from TRUD data
     *
     * Usage
     * =================================================================================
     * 1. Download the ICD10 data files from https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/28/subpack/258/releases#release-ICD_10.5.0_20151102000001
     * 2. Open the zip file
     * 3. Locate the "codes and titles and metadata file" e.g. ICD10_Edition5_CodesAndTitlesAndMetadata_GB_20160401.txt
     * 4. Then run this utility as:
     *      Main icd10 <names and titles and metadata file.txt>
     *
     * Verson on AWS Live is 2 November 2015
     */
    public static void updateIcd10Lookup(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: icd10 <icd10 txt file>");
            return;
        }

        LOG.info("ICD10 Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
        }

        saveIcd10Lookups(file);
        LOG.info("Finished ICD10 Import");
    }

    private static void saveIcd10Lookups(File file) throws Exception {

        Map<String, String> codeMap = readIcd10Records(file);
        int done = 0;

        Icd10DalI dal = DalProvider.factoryIcd10Dal();

        for (String code: codeMap.keySet()) {
            String description = codeMap.get(code);

            dal.updateIcd10Lookup(code, description);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " ICD10 lookups (out of approx 18k)");
            }
        }

        LOG.info("Done " + done + " ICD10 lookups");
    }

    /**
     * the file is simply five columns delimited with a tab. No column headers.
     */
    private static Map<String, String> readIcd10Records(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.TDF;

        CSVParser parser = null;
        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String code = record.get(0);
                String name = record.get(4);
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
