package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.Icd10DalI;
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

public class Icd10Updater {
    private static final Logger LOG = LoggerFactory.getLogger(OnsLsoaHelper.class);

    /**
     * updates the icd10_lookup table in the reference DB from TRUD data
     *
     * Usage
     * =================================================================================
     * 1. Download the ICD10 data files from https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/28/subpack/258/releases#release-ICD_10.5.0_20151102000001
     * 4. Then run this utility as:
     *      Main icd10 <file zip path>
     *
     * Verson on AWS Live is 2 November 2015
     */
    public static void updateIcd10Lookup(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: icd10 <icd10 zip file>");
            return;
        }

        LOG.info("ICD10 Update Starting");

        File file = new File(args[1]);

        if (!file.exists()) {
            LOG.error("" + file + " doesn't exist");
            return;
        }
        if (!ZipHelper.isZip(file)) {
            LOG.error("" + file + " isn't a zip file");
            return;
        }

        ZipInputStream zis = null;
        Reader r = null;

        LOG.info("Looking for Codes and Titles File...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, ".*/Content/.*_CodesAndTitlesAndMetadata_.*.txt");
        if (r == null) {
            LOG.error("Failed to find titles and metadata file");
            return;
        }
        Map<String, String> codeMap = readIcd10Records(r);
        LOG.info("Saving to DB...");
        saveIcd10Lookups(codeMap);
        zis.close();

        LOG.info("Looking for Equivalencies File...");
        zis = ZipHelper.createZipInputStream(file);
        r = ZipHelper.findFile(zis, ".*/Content/.*_TableOfCodingEquivalencesWithDescriptionsForward_.*.txt");
        if (r == null) {
            LOG.error("Failed to find titles and metadata file");
            return;
        }
        codeMap = readIcd10EquivalenceRecords(r);
        LOG.info("Saving to DB...");
        saveIcd10Lookups(codeMap);
        zis.close();

        LOG.info("Finished ICD10 Import");
    }


    private static void saveIcd10Lookups(Map<String, String> codeMap) throws Exception {

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
    private static Map<String, String> readIcd10Records(Reader r) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.TDF.withHeader();

        CSVParser parser = null;
        try {
            parser = new CSVParser(r, format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String code = record.get("CODE");
                String name = record.get("DESCRIPTION");
                map.put(code, name);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }

    private static Map<String, String> readIcd10EquivalenceRecords(Reader r) throws Exception {
        Map<String, String> map = new HashMap<>();

        CSVFormat format = CSVFormat.TDF.withHeader();

        CSVParser parser = null;
        try {
            parser = new CSVParser(r, format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String code = record.get("From: ICD-10 4th Edition code");
                String name = record.get("Code Description");
                String isEquivalent = record.get("Target Different to Source?");

                //this file contains all the codes, not just those that have equivalents. The ones that are equivalents
                //have this field as Y
                if (isEquivalent.equalsIgnoreCase("Y")) {
                    map.put(code, name);
                }
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }

}
