package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.utilities.CsvSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmisCsvFileSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvFileSplitter.class);

    private static final String SPLIT_COLUMN_ORG = "OrganisationGuid";
    private static final String SPLIT_COLUMN_PROCESSING_ID = "ProcessingId";

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;
    private static final Set<String> FILES_TO_SPLIT_BY_PROCESSING_ID = new HashSet<>();
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
    }

    public static List<File> splitFiles(String srcDir, String dstDir, DbConfiguration dbConfiguration) throws Exception {
        return splitFiles(new File(srcDir), new File(dstDir), dbConfiguration);
    }

    /**
     * splits the 17 EMIS extract files we use by org GUID and processing ID, so
     * we have a directory structure of dstDir -> org GUID -> processing ID
     * returns a list of directories containing split file sets
     */
    public static List<File> splitFiles(File srcDir, File dstDir, DbConfiguration dbConfiguration) throws Exception {

        LOG.trace("Splitting CSV files in {}", srcDir);

        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source directory " + srcDir + " doesn't exist");
        }

        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new FileNotFoundException("Failed to create destination directory " + dstDir);
            }
        }

        //scan through the files in the folder and works out which are admin and which are clinical
        List<File> adminFiles = new ArrayList<>();
        List<File> clinicalFiles = new ArrayList<>();
        identifyFiles(srcDir, adminFiles, clinicalFiles, dbConfiguration);

        //split the clinical files by org and processing ID
        for (File f: clinicalFiles) {
            LOG.trace("Splitting {} into {}", f, dstDir);
            splitFile(f, dstDir, CSV_FORMAT, SPLIT_COLUMN_ORG, SPLIT_COLUMN_PROCESSING_ID);
        }

        //for each org folder we want a copy of the non-clinical data, so split those files for each org
        for (File orgDir : dstDir.listFiles()) {

            for (File f: adminFiles) {
                LOG.trace("Splitting {} into {}", f, dstDir);
                splitFile(f, orgDir, CSV_FORMAT, SPLIT_COLUMN_PROCESSING_ID);
            }
        }

        //the splitter only creates files when required, so we'll have incomplete file sets,
        //so create any missing files, so there's a full set of files in every folder
        for (File f: srcDir.listFiles()) {
            createMissingFiles(f, dstDir);
        }

        LOG.trace("Completed CSV file splitting from {} to {}", srcDir, dstDir);

        //build a list of the folders containing file sets, to return
        List<File> ret = new ArrayList<>();

        for (File orgDir : dstDir.listFiles()) {
            for (File processingDir : orgDir.listFiles()) {
                ret.add(processingDir);
            }
        }

        return ret;
    }

    /**
     * scans through the files in the folder and works out which are admin and which are clinical
     */
    private static void identifyFiles(File srcDir, List<File> adminFiles, List<File> clinicalFiles, DbConfiguration dbConfiguration) throws Exception {

        for (File f: srcDir.listFiles()) {

            EmisSftpFilenameParser parser = new EmisSftpFilenameParser(f.getName(), dbConfiguration);
            String fileType = parser.generateFileTypeIdentifier();
            if (FILES_TO_SPLIT_BY_PROCESSING_ID.contains(fileType)) {
                adminFiles.add(f);
            } else if (FILES_TO_SPLIT_BY_ORG_AND_PROCESSING_ID.contains(fileType)) {
                clinicalFiles.add(f);
            } else {
                throw new SftpFilenameParseException("Unknown EMIS CSV file type for " + f);
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
