package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.sftpreader.DataLayer;
import org.endeavourhealth.sftpreader.implementations.SftpBatchSplitter;
import org.endeavourhealth.sftpreader.model.db.*;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.utilities.CsvSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class EmisSftpBatchSplitter extends SftpBatchSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(EmisSftpBatchSplitter.class);

    private static final String SPLIT_COLUMN_ORG = "OrganisationGuid";
    private static final String SPLIT_COLUMN_PROCESSING_ID = "ProcessingId";

    private static final String SPLIT_FOLDER = "Split";

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;
    private static final Set<String> FILES_TO_SPLIT_BY_PROCESSING_ID = new HashSet<>();
    private static final Set<String> FILES_TO_SPLIT_BY_ORG_ID = new HashSet<>();
    private static final Set<String> FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID = new HashSet<>();

    static {
        //these files only contain a processing_id
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Admin_Location");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Admin_Organisation");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Admin_OrganisationLocation");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Admin_UserInRole");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Appointment_Session");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Appointment_SessionUser");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Coding_ClinicalCode");
        FILES_TO_SPLIT_BY_PROCESSING_ID.add("Coding_DrugCode");

        //these files contain a processing_id and organisation GUID
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("Admin_Patient");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("Appointment_Slot");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("CareRecord_Consultation");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("CareRecord_Diary");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("CareRecord_Observation");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("CareRecord_ObservationReferral");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("CareRecord_Problem");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("Prescribing_DrugRecord");
        FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.add("Prescribing_IssueRecord");

        //this file only has an organisation_guid
        FILES_TO_SPLIT_BY_ORG_ID.add(("Agreements_SharingOrganisation"));
    }

    /**
     * splits the 17 EMIS extract files we use by org GUID and processing ID, so
     * we have a directory structure of dstDir -> org GUID -> processing ID
     * returns a list of directories containing split file sets
     */
    @Override
    public List<BatchSplit> splitBatch(Batch batch, DataLayer db, DbConfiguration dbConfiguration) throws Exception {

        String path = FilenameUtils.concat(dbConfiguration.getLocalRootPath(), batch.getLocalRelativePath());
        File srcDir = new File(path);

        LOG.trace("Splitting CSV files in {}", srcDir);

        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source directory " + srcDir + " doesn't exist");
        }

        //split into a sub-folder called "split"
        path = FilenameUtils.concat(path, SPLIT_FOLDER);
        File dstDir = new File(path);

        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new FileNotFoundException("Failed to create destination directory " + dstDir);
            }
        }

        //scan through the files in the folder and works out which are admin and which are clinical
        List<String> processingIdFiles = new ArrayList<>();
        List<String> orgAndProcessingIdFiles = new ArrayList<>();
        List<String> orgIdFiles = new ArrayList<>();
        identifyFiles(batch, orgAndProcessingIdFiles, processingIdFiles, orgIdFiles, dbConfiguration);

        //split the clinical files by org and processing ID, which creates the org ID -> processing ID folder structure
        for (String fileName: orgAndProcessingIdFiles) {
            File f = new File(srcDir, fileName);
            LOG.trace("Splitting {} into {}", f, dstDir);
            splitFile(f, dstDir, CSV_FORMAT, SPLIT_COLUMN_ORG, SPLIT_COLUMN_PROCESSING_ID);
        }

        //for the files with just a processing ID, each org folder we want a copy of the non-clinical data, so split those files for each org
        for (File orgDir : dstDir.listFiles()) {

            for (String fileName : processingIdFiles) {
                File f = new File(srcDir, fileName);
                LOG.trace("Splitting {} into {}", f, orgDir);
                splitFile(f, orgDir, CSV_FORMAT, SPLIT_COLUMN_PROCESSING_ID);
            }
        }

        //for the file with just an org ID, splitting by org ID will leave the split copies in the org ID folders,
        //which we then need to copy into the org ID -> processing ID sub-folders
        for (String fileName: orgIdFiles) {
            File f = new File(srcDir, fileName);
            LOG.trace("Splitting {} into {}", f, dstDir);
            splitFile(f, dstDir, CSV_FORMAT, SPLIT_COLUMN_ORG);

            for (File orgDir : dstDir.listFiles()) {
                f = new File(orgDir, fileName);

                for (File processingDir : orgDir.listFiles()) {
                    if (processingDir.isDirectory()) {

                        File dst = new File(processingDir, fileName);

                        Files.copy(f.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        //Files.copy(f.toPath(), dst.toPath());
                    }
                }

                f.delete();

                //the sharing agreements file always has a row per org in the data sharing agreement, even if there
                //isn't any data for that org in the extract. So we'll have just created a folder for that org
                //and it'll now be empty. So delete any empty org directory.
                if (orgDir.listFiles().length == 0) {
                    orgDir.delete();
                }
            }
        }

        //the splitter only creates files when required, so we'll have incomplete file sets,
        //so create any missing files, so there's a full set of files in every folder
        for (BatchFile batchFile: batch.getBatchFiles()) {
            File f = new File(srcDir, batchFile.getDecryptedFilename());
            createMissingFiles(f, dstDir);
        }

        LOG.trace("Completed CSV file splitting from {} to {}", srcDir, dstDir);

        //build a list of the folders containing file sets, to return
        List<BatchSplit> ret = new ArrayList<>();

        for (File orgDir : dstDir.listFiles()) {

            String orgId = orgDir.getName();
            String localPath = FilenameUtils.concat(batch.getLocalRelativePath(), SPLIT_FOLDER);
            localPath = FilenameUtils.concat(localPath, orgId);

            //we need to find the ODS code for the EMIS org GUID. When we have a full extract, we can find that mapping
            //in the Organisation CSV file, but for deltas, we use the key-value-pair table which is populated when we get the deltas
            String odsCode = findOdsCode(orgId, db, dbConfiguration, batch);

            BatchSplit batchSplit = new BatchSplit();
            batchSplit.setBatchId(batch.getBatchId());
            batchSplit.setLocalRelativePath(localPath);
            batchSplit.setOrganisationId(odsCode);

            ret.add(batchSplit);
        }

        return ret;
    }

    private static String findOdsCode(String emisOrgGuid, DataLayer db, DbConfiguration dbConfiguration, Batch batch) throws Exception {

        //first look in our key-value-pair table, as any previously encountered orgs will have been stored in there
        for (DbConfigurationKvp kvp: dbConfiguration.getDbConfigurationKvp()) {
            if (kvp.getKey().equalsIgnoreCase(emisOrgGuid)) {
                return kvp.getValue();
            }
        }

        //if not found in the key-value-pair table, then we need to process the Organisation CSV file to find it
        File adminCsvFile = null;
        for (BatchFile batchFile: batch.getBatchFiles()) {
            if (batchFile.getFileTypeIdentifier().equalsIgnoreCase("Admin_Organisation")) {
                String path = FilenameUtils.concat(dbConfiguration.getLocalRootPath(), batch.getLocalRelativePath());
                path = FilenameUtils.concat(path, batchFile.getDecryptedFilename());
                adminCsvFile = new File(path);
            }
        }

        CSVParser csvParser = CSVParser.parse(adminCsvFile, Charset.defaultCharset(), CSV_FORMAT.withHeader());
        try {
            Iterator<CSVRecord> csvIterator = csvParser.iterator();

            while (csvIterator.hasNext()) {
                CSVRecord csvRecord = csvIterator.next();
                String orgGuid = csvRecord.get("OrganisationGuid");
                if (orgGuid.equalsIgnoreCase(emisOrgGuid)) {
                    String orgOds = csvRecord.get("ODSCode");

                    DbConfigurationKvp newKvp = new DbConfigurationKvp();
                    newKvp.setKey(emisOrgGuid);
                    newKvp.setValue(orgOds);
                    dbConfiguration.getDbConfigurationKvp().add(newKvp);

                    //save the new pair for next time
                    db.addConfigurationKvp(newKvp, dbConfiguration.getInstanceId());

                    return orgOds;
                }

            }
        } finally {
            csvParser.close();
        }

        throw new RuntimeException("Failed to find ODS code for EMIS Org GUID " + emisOrgGuid);
    }

    /**
     * scans through the files in the folder and works out which are admin and which are clinical
     */
    private static void identifyFiles(Batch batch, List<String> orgAndProcessingIdFiles, List<String> processingIdFiles,
                                      List<String> orgIdFiles, DbConfiguration dbConfiguration) throws Exception {

        for (BatchFile batchFile: batch.getBatchFiles()) {

            String fileName = batchFile.getDecryptedFilename();
            EmisSftpFilenameParser parser = new EmisSftpFilenameParser(fileName, dbConfiguration, ".csv");
            String fileType = parser.generateFileTypeIdentifier();
            if (FILES_TO_SPLIT_BY_PROCESSING_ID.contains(fileType)) {
                processingIdFiles.add(fileName);
            } else if (FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.contains(fileType)) {
                orgAndProcessingIdFiles.add(fileName);
            } else if (FILES_TO_SPLIT_BY_ORG_ID.contains(fileType)) {
                orgIdFiles.add(fileName);
            } else {
                throw new SftpFilenameParseException("Unknown EMIS CSV file type for " + fileName);
            }
        }

    }

    private static void createMissingFiles(File srcFile, File dstDir) throws Exception {

        //read in the first line of the source file, as we use that as the content for the empty files
        String headers = readFileHeaders(srcFile);
        String fileName = srcFile.getName();

        //iterate through any directories, creating any missing files in their sub-directories
        for (File orgDir: dstDir.listFiles()) {
            if (orgDir.isDirectory()) {
                for (File processingDir: orgDir.listFiles()) {
                    if (processingDir.isDirectory()) {
                        createMissingFile(fileName, headers, processingDir);
                    }
                }
            }
        }
    }
    private static void createMissingFile(String fileName, String headers, File dstDir) throws Exception {

        File dstFile = new File(dstDir, fileName);
        if (dstFile.exists()) {
            return;
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {

            fileWriter = new FileWriter(dstFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.append(headers);
            bufferedWriter.newLine();

        } finally {

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    private static String readFileHeaders(File srcFile) throws Exception {

        String headers = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(srcFile));
            headers = bufferedReader.readLine();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return headers;
    }


    private static void splitFile(File srcFile, File dstDir, CSVFormat csvFormat, String... splitColmumns) throws Exception {
        CsvSplitter csvSplitter = new CsvSplitter(srcFile, dstDir, csvFormat, splitColmumns);
        csvSplitter.go();
    }


    /*private static void copyFile(String partialFileName, File srcDir, File dstDir) throws Exception {

        String[] arr = partialFileName.split("_");
        String domain = arr[0];
        String name = arr[1];

        File srcFile = EmisCsvTransformer.getFileByPartialName(domain, name, srcDir);
        File dstFile = new File(dstDir, srcFile.getName());

        //this uses a 4k buffer for copying. This may prove too slow, and need re-implementing to use a larger buffer
        Files.copy(srcFile, dstFile);
    }*/

}
