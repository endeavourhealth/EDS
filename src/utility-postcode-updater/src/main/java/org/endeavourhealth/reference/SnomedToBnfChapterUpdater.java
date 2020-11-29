package org.endeavourhealth.reference;

import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.SnomedToBnfChapterDalI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SnomedToBnfChapterUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedToBnfChapterUpdater.class);

    private static final String BNF_SNOMED_LOCAL_PATH = "bnf_snomed_local_path";

    private static String getConfig(String config) throws Exception {
        return ConfigManager.getConfiguration(config);
    }

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
        try {
            SnomedAndBnfConnector snomedAndBnfConnector = new SnomedAndBnfConnector();
            SnomedAndBnfExcelReader snomedAndBnfExcelReader = new SnomedAndBnfExcelReader();

            String bnfSNOMEDLocalPath = getConfig(BNF_SNOMED_LOCAL_PATH); //config.config record: global	bnf_snomed_local_path	D:\\NHS\\Data\\test\\
            if (bnfSNOMEDLocalPath == null) {
                LOG.error("Local pith path is not configured in DB exist.");
            }
            //download Snomed To BNF mapping file zip locally
            String zipFileName = snomedAndBnfConnector.downloadBNFAndSNOMEDMappingFile(bnfSNOMEDLocalPath);
            //decode the file name (BNF%20Snomed%20Mapping%20data%2020201116.zip to BNF Snomed Mapping data 20201116.zip)
            String zipFileNameDecoded = URLDecoder.decode(zipFileName, "UTF-8");
            LOG.info("decoded zip file name: " + zipFileNameDecoded);

            String csvfileName = null;
            String xlsxFileName = null;
            ZipFile zipFile = new ZipFile(bnfSNOMEDLocalPath + zipFileName);
            //extract Snomed To BNF mapping xlsx file from downloaded zip file
            zipFile.extractAll(bnfSNOMEDLocalPath);

            File file = new File(bnfSNOMEDLocalPath + zipFileName);
            Date dataDate = null;
            if (!file.exists()) {
                LOG.error("" + file + " doesn't exist.");
            }
            else {
                //get the date from file name (BNF Snomed Mapping data 20201216.zip : Mon Nov 16 00:00:00 GMT 2020)
                dataDate = parseDate(zipFileNameDecoded);
            }
            if (zipFileNameDecoded != null && zipFileNameDecoded.length() > 0) {
                xlsxFileName = zipFileNameDecoded.replace(".zip", ".xlsx");
                csvfileName = zipFileNameDecoded.replace(".zip", ".csv");
                //create CSV file from the XLSX file retaining just the columns BNF Code and SNOMED Code
                //snomedAndBnfExcelReader.createCSV(bnfSNOMEDLocalPath + xlsxFileName, bnfSNOMEDLocalPath + csvfileName);
                snomedAndBnfExcelReader.createCSV(bnfSNOMEDLocalPath + "BNF_Snomed_Mapping_data_20201021_slim.xlsx", bnfSNOMEDLocalPath + csvfileName); //Testing
                //load the csv file to snomed_to_bnf_chapter_lookup table in the reference DB
                saveSnomedToBnfLookups(bnfSNOMEDLocalPath + csvfileName, dataDate);
            }
        } catch (Exception e) {
            LOG.error("Error while processing Snomed To BNF Chapter Import.");
        }
        LOG.info("Finished Snomed To BNF Chapter Import.");
    }

    /*
     * parse the date from file name (BNF Snomed Mapping data 20201216.zip : Mon Nov 16 00:00:00 GMT 2020)
     */
    private static Date parseDate(String fileName) throws Exception {
        Date dataDate = null;
        String strDate = null;
        try {
            //fileName = "BNF Snomed Mapping data 20201216.zip"; //for testing
            int index = fileName.lastIndexOf(' '); //file name is space separated
            strDate = fileName.substring(index + 1, fileName.length() - 4); //BNF Snomed Mapping data 20201116.zip
            LOG.info("date in file : " + strDate);
            if(strDate != null && strDate.length() > 0) {
                strDate =  strDate.substring(0, 4).concat("-").concat(strDate.substring(4, 6)).concat("-").concat(strDate.substring(6, 8));//20201116 to 2020-11-16
                dataDate = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
                LOG.info("dataDate : " + dataDate);
            }
        } catch (Exception e) {
            LOG.error("Error while parsing the date from file name");
        }
        return dataDate;
    }
    /*
     * load the csv file to snomed_to_bnf_chapter_lookup table in the reference DB
     */
    private static void saveSnomedToBnfLookups(String filePath, Date dataDate) throws Exception {
        LOG.info(" Start saveSnomedToBnfLookups()");
        SnomedToBnfChapterDalI dal = DalProvider.factorySnomedToBnfChapter();
        dal.updateSnomedToBnfChapterLookup(filePath, dataDate);
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
