package org.endeavourhealth.reference;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.sqlserver.jdbc.StringUtils;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.SnomedToBnfChapterDalI;
import org.endeavourhealth.reference.helpers.SnomedAndBnfConnector;
import org.endeavourhealth.reference.helpers.SnomedAndBnfExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SnomedToBnfChapterUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedToBnfChapterUpdater.class);

    /**
     * This utility would :
     * 1. connect BNF website https://www.nhsbsa.nhs.uk/prescription-data/understanding-our-data/bnf-snomed-mapping
     * 2. download the zip file
     * 3. extract the zip file locally
     * 4. use Apache POI to convert the .xlsx extract to .csv retaining just the columns "SNOMED Code" and "BNF Code"
     * 5. load the csv to database
     * 6. updates the snomed_to_bnf_chapter_lookup table in the reference DB from NHS BSA data
     * Usage
     * =================================================================================
     * run this utility as:
     *    Main snomedToBnfChapter
     *    VM options -Xmx8048m
     */
    public static void updateSnomedToBnfChapterLookup(String[] args) throws Exception {
        LOG.info("Snomed To BNF Chapter Update Starting.");

        String localPath = null;
        String csvfileName = null;
        String xlsxFileName = null;
        String zipFileName = null;

        try {

            JsonNode config = ConfigManager.getConfigurationAsJson("bnf_snomed","reference");
            if (config == null) {
                LOG.error("Config not found app_id = 'reference', config_id = 'bnf_snomed'");
                throw new Exception("Config not found app_id = 'reference', config_id = 'bnf_snomed'");
            }
            String websiteURL = config.get("web_site_url").textValue();
            if (StringUtils.isEmpty(websiteURL)) {
                LOG.error("Required config not found: web_site_url");
                throw new Exception("Required config not found: web_site_url");
            }
            String baseURL = config.get("zip_file_base_url").textValue();
            if (StringUtils.isEmpty(baseURL)) {
                LOG.error("Required config not found: zip_file_base_url");
                throw new Exception("Required config not found: zip_file_base_url");
            }
            localPath = config.get("local_path").textValue();
            if (StringUtils.isEmpty(localPath)) {
                LOG.error("Required config not found: local_path");
                throw new Exception("Required config not found: local_path");
            }
            File path = new File(localPath);
            if (!path.exists()) {
                FileUtils.forceMkdir(path);
            }
            String bnfColumn = config.get("bnf_column").textValue();
            if (StringUtils.isEmpty(localPath)) {
                LOG.error("Required config not found: bnf_column");
                throw new Exception("Required config not found: bnf_column");
            }
            String snomedColumn = config.get("snomed_column").textValue();
            if (StringUtils.isEmpty(localPath)) {
                LOG.error("Required config not found: snomed_column");
                throw new Exception("Required config not found: snomed_column");
            }

            SnomedAndBnfConnector snomedAndBnfConnector = new SnomedAndBnfConnector();
            SnomedAndBnfExcelReader snomedAndBnfExcelReader = new SnomedAndBnfExcelReader();

            //download Snomed To BNF mapping file zip locally
            zipFileName = snomedAndBnfConnector.downloadBNFAndSNOMEDMappingFile(websiteURL, localPath, baseURL);

            //decode the file name
            String zipFileNameDecoded = URLDecoder.decode(zipFileName, "UTF-8");
            LOG.info("decoded zip file name: " + zipFileNameDecoded);

            ZipFile zipFile = new ZipFile(localPath + zipFileName);

            //extract Snomed To BNF mapping xlsx file from downloaded zip file
            zipFile.extractAll(localPath);

            File file = new File(localPath + zipFileName);
            if (!file.exists()) {
                LOG.error("" + file + " doesn't exist.");
                throw new Exception("" + file + " doesn't exist.");
            }
            if (zipFileNameDecoded != null && zipFileNameDecoded.length() > 0) {
                xlsxFileName = zipFileNameDecoded.replace(".zip", ".xlsx");
                csvfileName = zipFileNameDecoded.replace(".zip", ".csv");
                //create CSV file from the XLSX file retaining just the columns BNF Code and SNOMED Code
                snomedAndBnfExcelReader.createCSV(localPath + xlsxFileName,
                        localPath + csvfileName, bnfColumn, snomedColumn);
                //load the csv file to snomed_to_bnf_chapter_lookup table in the reference DB
                saveSnomedToBnfLookups(localPath + csvfileName);

            } else {
                LOG.error("Unable to get the zip file.");
                throw new Exception("Unable to get the zip file.");
            }
        } catch (Exception e) {
            LOG.error("Error while processing Snomed To BNF Chapter Import.");
            throw e;
        } finally {
            if (!StringUtils.isEmpty(localPath)) {
                try {
                    FileUtils.forceDelete(new File(localPath + xlsxFileName));
                    FileUtils.forceDelete(new File(localPath + csvfileName));
                    FileUtils.forceDelete(new File(localPath + zipFileName));
                } catch (Exception e) {
                }
            }
        }
        LOG.info("Finished Snomed To BNF Chapter Import.");
    }

    /*
     * load the csv file to snomed_to_bnf_chapter_lookup table in the reference DB
     */
    private static void saveSnomedToBnfLookups(String filePath) throws Exception {
        LOG.info(" Start saveSnomedToBnfLookups()");
        SnomedToBnfChapterDalI dal = DalProvider.factorySnomedToBnfChapter();
        dal.updateSnomedToBnfChapterLookup(filePath);
        LOG.info(" End saveSnomedToBnfLookups()");
    }

  /*  private static void saveSnomedToBnfLookups(File file) throws Exception {
        Map<String, String> codeMap = readSnomedToBnfChapterRecords(file);
        int done = 0;

        SnomedToBnfChapterDalI dal = DalProvider.factorySnomedToBnfChapter();

        for (String snomedCode: codeMap.keySet()) {
            String bnfChapterCode = codeMap.get(snomedCode);

            dal.updateSnomedToBnfChapterLookup(snomedCode, bnfChapterCode);

            done ++;
            if (done % 1000 == 0) {
                LOG.info("Done " + done + " Snomed To BNF Chapter Lookups (out of approx 325k).");
            }
        }

        LOG.info("Done " + done + " Snomed To BNF Chapter Lookups");
    }
*/

    /**
     * the .csv file should be two columns delimited with a comma, with column headers.
     */
    private static Map<String, String> readSnomedToBnfChapterRecords(File src) throws Exception {
        Map<String, String> map = new HashMap<>();

        // CSVFormat format = CSVFormat.DEFAULT;

        CSVFormat format = CSVFormat.newFormat(',')
                .withFirstRecordAsHeader()
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true);

        CSVParser parser = null;

        try {
            parser = CSVParser.parse(src, Charset.defaultCharset(), format);
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String snomedCode = record.get(0);
                String bnfChapterCode = record.get(1);
                map.put(snomedCode, bnfChapterCode);
            }

        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return map;
    }
}
