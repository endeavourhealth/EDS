package org.endeavourhealth.transform.emis.csv;

import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvSplitter;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class EmisCsvFileSplitter {

    private static final String SPLIT_COLUMN = "OrganisationGuid";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;
    private static final Set<String> FILES_TO_COPY = new HashSet<>();
    private static final Set<String> FILES_TO_SPLIT = new HashSet<>();

    static {
        FILES_TO_COPY.add("Admin_Location");
        FILES_TO_COPY.add("Admin_Organisation");
        FILES_TO_COPY.add("Admin_OrganisationLocation");
        FILES_TO_COPY.add("Admin_UserInRole");
        FILES_TO_COPY.add("Agreements_SharingOrganisation");
        FILES_TO_COPY.add("Appointment_Session");
        FILES_TO_COPY.add("Appointment_SessionUser");
        FILES_TO_COPY.add("Coding_ClinicalCode");
        FILES_TO_COPY.add("Coding_DrugCode");

        FILES_TO_SPLIT.add("Admin_Patient");
        FILES_TO_SPLIT.add("Appointment_Slot");
        FILES_TO_SPLIT.add("CareRecord_Consultation");
        FILES_TO_SPLIT.add("CareRecord_Diary");
        FILES_TO_SPLIT.add("CareRecord_Observation");
        FILES_TO_SPLIT.add("CareRecord_ObservationReferral");
        FILES_TO_SPLIT.add("CareRecord_Problem");
        FILES_TO_SPLIT.add("Prescribing_DrugRecord");
        FILES_TO_SPLIT.add("Prescribing_IssueRecord");
    }

    public static void split(String srcDir, String dstDir) throws Exception {
        split(new File(srcDir), new File(dstDir));
    }

    public static void split(File srcDir, File dstDir) throws Exception {

        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source directory " + srcDir + " doesn't exist");
        }

        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new FileNotFoundException("Failed to create destination directory " + dstDir);
            }
        }

        //simply copy the files we don't want to split
        for (String fileName: FILES_TO_COPY) {
            copyFile(fileName, srcDir, dstDir);
        }

        //split the files we know should be split
        for (String fileName: FILES_TO_SPLIT) {
            splitFile(fileName, srcDir, dstDir, CSV_FORMAT);
        }

        //finally create any missing files, so there's a full set of files in every folder
        for (String fileName: FILES_TO_COPY) {
            createMissingFiles(fileName, srcDir, dstDir);
        }
        for (String fileName: FILES_TO_SPLIT) {
            createMissingFiles(fileName, srcDir, dstDir);
        }

        //if there's any files left in the source folder, just copy them to the destination folder,
        //so we can delete the source folder without having lost anything, even if we didn't expect it
        for (File remainingFile: srcDir.listFiles()) {
            if (remainingFile.isFile()) {
                String fileName = remainingFile.getName();
                File dstFile = new File(dstDir, fileName);
                if (!dstFile.exists()) {
                    Files.copy(remainingFile, dstFile);
                }
            }
        }
    }

    private static void createMissingFiles(String partialFileName, File srcDir, File dstDir) throws Exception {

        File srcFile = EmisCsvTransformer.getFileByPartialName(partialFileName, srcDir);
        String fileName = srcFile.getName();

        //read in the first line of the source file, as we use that as the content for the empty files
        String headers = readFileHeaders(srcFile);

        createMissingFile(fileName, headers, dstDir);

        for (File child: dstDir.listFiles()) {
            if (child.isDirectory()) {
                createMissingFile(fileName, headers, child);
            }
        }
    }
    private static void createMissingFile(String fileName, String headers, File dstDir) throws Exception {

        File dstFile = new File(dstDir, fileName);
        if (dstFile.exists()) {
            return;
        }

        BufferedWriter bufferedWriter = null;
        try {

            bufferedWriter = new BufferedWriter(new FileWriter(dstFile));
            bufferedWriter.append(headers);
            bufferedWriter.newLine();

        } finally {

            if (bufferedWriter != null) {
                bufferedWriter.close();
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


    private static void splitFile(String partialFileName, File srcDir, File dstDir, CSVFormat csvFormat) throws Exception {

        File srcFile = EmisCsvTransformer.getFileByPartialName(partialFileName, srcDir);

        CsvSplitter csvSplitter = new CsvSplitter(srcFile, dstDir, csvFormat, SPLIT_COLUMN);
        csvSplitter.go();
    }

    private static void copyFile(String partialFileName, File srcDir, File dstDir) throws Exception {

        File srcFile = EmisCsvTransformer.getFileByPartialName(partialFileName, srcDir);
        File dstFile = new File(dstDir, srcFile.getName());

        //this uses a 4k buffer for copying. This may prove too slow, and need re-implementing to use a larger buffer
        Files.copy(srcFile, dstFile);
    }

}
