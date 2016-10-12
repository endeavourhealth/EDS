package org.endeavourhealth.transform.emis;

import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.core.xml.TransformErrorsSerializer;
import org.endeavourhealth.core.xml.transformErrors.TransformError;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.admin.*;
import org.endeavourhealth.transform.emis.csv.schema.agreements.SharingOrganisation;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Session;
import org.endeavourhealth.transform.emis.csv.schema.appointment.SessionUser;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Slot;
import org.endeavourhealth.transform.emis.csv.schema.audit.PatientAudit;
import org.endeavourhealth.transform.emis.csv.schema.audit.RegistrationAudit;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.*;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.coding.DrugCode;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord;
import org.endeavourhealth.transform.emis.csv.transforms.admin.*;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionUserTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.*;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.DrugCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordPreTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordPreTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public abstract class EmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformer.class);

    public static final String VERSION_5_1 = "5.1";
    public static final String VERSION_TEST_PACK = "TestPack";

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data is different
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;


    public static List<UUID> transform(String version,
                                       String sharedStoragePath,
                                       String[] files,
                                       UUID exchangeId,
                                       UUID serviceId,
                                       UUID systemId,
                                       TransformError transformError) {

        LOG.info("Invoking EMIS CSV transformer for {} files", files.length);

        try {

            //validate the version
            validateVersion(version);

            //the files should all be in a directory structure of org folder -> processing ID folder -> CSV files
            File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);

            //the processor is responsible for saving FHIR resources
            CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId, transformError);

            for (File processingIdDirectory : getProcessingIdDirectoriesInOrder(orgDirectory)) {

                Map<Class, AbstractCsvParser> parsers = new HashMap<>();

                try {
                    //validate that we've got all the files we expect
                    LOG.trace("Validating all files are present in {}", processingIdDirectory);
                    validateAndOpenParsers(processingIdDirectory, version, parsers);

                    //validate there's no additional files in the common directory
                    LOG.trace("Validating no additional files in {}", processingIdDirectory);
                    validateNoExtraFiles(processingIdDirectory, parsers);

                    LOG.trace("Transforming EMIS CSV content in {}", processingIdDirectory);
                    transformProcessingIdFolder(version, parsers, processor);

                } catch (Exception ex) {

                    logTransformProcessingIdError(transformError, ex, processingIdDirectory.getName());

                    //if we've had an error at this level, something is fundamentally wrong with the files we've
                    //received, and it's risky to continue processing any other processing ID folders, so break out
                    break;

                } finally {

                    closeParsers(parsers);
                }
            }

            LOG.trace("Completed transform for organisation {} - waiting for resources to commit to DB", orgDirectory.getName());
            return processor.getBatchIdsCreated();

        } catch (Exception ex) {

            //if the version is wrong, or the files aren't where they're supposed to be, we'll
            //have a fatal error, that means we won't have processed any data or created any batch IDs
            logTransformFataldError(transformError, ex);
            return null;
        }

    }


    /**
     * called if we get an error within a processing ID folder (e.g. can't open a file or file columns wrong)
     */
    public static void logTransformFataldError(TransformError transformError, Exception ex) {

        LOG.error("", ex);

        //then add the error to our audit object
        Map<String, String> args = new HashMap<>();
        args.put(TransformErrorsSerializer.ARG_EMIS_CSV_FATAL_ERROR, ex.getMessage());

        TransformErrorsSerializer.addError(transformError, ex, args);
    }

    /**
     * called if we get an error within a processing ID folder (e.g. can't open a file or file columns wrong)
     */
    public static void logTransformProcessingIdError(TransformError transformError, Exception ex, String processingId) {

        LOG.error("Error with processing ID " + processingId, ex);

        //then add the error to our audit object
        Map<String, String> args = new HashMap<>();
        args.put(TransformErrorsSerializer.ARG_EMIS_CSV_PROCESSING_ID, processingId);

        TransformErrorsSerializer.addError(transformError, ex, args);
    }

    private static void closeParsers(Map<Class, AbstractCsvParser> parsers) {

        for (AbstractCsvParser parser: parsers.values()) {
            try {
                parser.close();
            } catch (IOException ex) {
                //don't worry if this fails, as we're done anyway
            }
        }

    }

    private static List<File> getProcessingIdDirectoriesInOrder(File rootDir) {

        //the org directory contains a sub-directory for each processing ID, which must be processed in order
        List<Integer> processingIds = new ArrayList<>();
        Map<Integer, File> hmFiles = new HashMap<>();
        for (File file: rootDir.listFiles()) {
            Integer processingId = Integer.valueOf(file.getName());
            processingIds.add(processingId);
            hmFiles.put(processingId, file);
        }

        Collections.sort(processingIds);

        List<File> ret = new ArrayList<>();

        for (Integer processingId: processingIds) {
            File f = hmFiles.get(processingId);
            ret.add(f);
        }

        return ret;
    }

    /*public static List<UUID> transform(String version,
                                       String sharedStoragePath,
                                       String[] files,
                                       UUID exchangeId,
                                       UUID serviceId,
                                       UUID systemId) throws Exception {

        LOG.info("Invoking EMIS CSV transformer for {} files", files.length);

        //the files should all be in a directory structure of org folder -> processing ID folder -> CSV files
        LOG.trace("Validating all files are in same directory");
        File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);

        //under the org directory should be a directory for each processing ID
        for (File processingIdDirectory: orgDirectory.listFiles()) {

            //validate that we've got all the files we expect
            LOG.trace("Validating all files are present in {}", processingIdDirectory);
            Map<Class, File> expectedFiles = validateExpectedFiles(processingIdDirectory, version);

            //validate there's no additional files in the common directory
            LOG.trace("Validating no additional files in {}", processingIdDirectory);
            validateNoExtraFiles(processingIdDirectory, expectedFiles);

            //validate the column names match what we expect (this is repeated when we actually perform
            //the transformProcessingIdFolder, but by doing it now, we fail earlier rather than later)
            LOG.trace("Validating CSV column names in {}", processingIdDirectory);
            validateColumnNames(expectedFiles, version);
        }

        //the org directory contains a sub-directory for each processing ID, which must be processed in order
        List<Integer> processingIds = new ArrayList<>();
        Map<Integer, File> hmFiles = new HashMap<>();
        for (File file: orgDirectory.listFiles()) {
            Integer processingId = Integer.valueOf(file.getName());
            processingIds.add(processingId);
            hmFiles.put(processingId, file);
        }

        Collections.sort(processingIds);

        //the processor is responsible for saving FHIR resources
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId);

        LOG.trace("Starting transformProcessingIdFolder for organisation {}", orgDirectory.getName());

        for (Integer processingId: processingIds) {
            File processingIdDir = hmFiles.get(processingId);
            transformProcessingIdFolder(version, processingIdDir, processor);
        }

        LOG.trace("Completed transfom for organisation {} - waiting for resources to commit to DB", orgDirectory.getName());

        return processor.getBatchIdsCreated();
    }*/

    /**
     * recursively empties the directory struture then deletes the directories
     * Java can delete directories, but the files have to be manually deleted first
     */
    /*private static void deleteDirectory(File root) {

        for (File f: root.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirectory(f);
            }
        }

        root.delete();
    }*/


    private static void validateVersion(String version) throws Exception {

        if (!version.equalsIgnoreCase(EmisCsvTransformer.VERSION_TEST_PACK)
                && !version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_1)) {
            throw new TransformException("Unsupported version for EMIS CSV: " + version);
        }
    }

    /**
     * validates that all the files in the array are in the expected directory structure of org folder -> processing ID folder
     */
    private static File validateAndFindCommonDirectory(String sharedStoragePath, String[] files) throws Exception {
        String organisationDir = null;

        for (String file: files) {
            File f = new File(sharedStoragePath, file);
            if (!f.exists()) {
                LOG.error("Failed to find file {} in shared storage {}", file, sharedStoragePath);
                throw new FileNotFoundException("" + f + " doesn't exist");
            }
            //LOG.info("Successfully found file {} in shared storage {}", file, sharedStoragePath);

            try {
                File processingIdDir = f.getParentFile();
                File orgDir = processingIdDir.getParentFile();

                if (organisationDir == null) {
                    organisationDir = orgDir.getAbsolutePath();
                } else {
                    if (!organisationDir.equalsIgnoreCase(orgDir.getAbsolutePath())) {
                        throw new Exception();
                    }
                }

            } catch (Exception ex) {
                throw new FileNotFoundException("" + f + " isn't in the expected directory structure within " + organisationDir);
            }

        }
        return new File(organisationDir);
    }

    /*private static void validateColumnNames(Map<Class, File> expectedFiles, String version) throws Exception {

        Iterator<Class> classIterator = expectedFiles.keySet().iterator();
        while (classIterator.hasNext()) {
            Class parserClass = classIterator.next();

            //simply invoke the same constructor that the transform classes use, which will validate the columns
            Constructor<AbstractCsvTransformer> constructor = parserClass.getConstructor(String.class, Map.class);
            AbstractCsvTransformer transformer = constructor.newInstance(version, expectedFiles);
            transformer.close();
        }
    }*/

    /**
     * validates that the directory contains ONLY the given files
     */
    private static void validateNoExtraFiles(File commonDir, Map<Class, AbstractCsvParser> parsers) throws Exception {

        Set<File> expectedFiles = parsers
                .values()
                .stream()
                .map(T -> T.getFile())
                .collect(Collectors.toSet());

        for (File file: commonDir.listFiles()) {
            if (file.isFile()
                && !expectedFiles.contains(file)
                && !Files.getFileExtension(file.getAbsolutePath()).equalsIgnoreCase("gpg")) {

                throw new FileFormatException(file, "Unexpected file " + file + " in EMIS CSV extract");
            }
        }
    }
    
    private static void validateAndOpenParsers(File dir, String version, Map<Class, AbstractCsvParser> parsers) throws Exception {

        findFileAndOpenParser(Location.class, dir, version, parsers);
        findFileAndOpenParser(Organisation.class, dir, version, parsers);
        findFileAndOpenParser(OrganisationLocation.class, dir, version, parsers);
        findFileAndOpenParser(Patient.class, dir, version, parsers);
        findFileAndOpenParser(UserInRole.class, dir, version, parsers);
        findFileAndOpenParser(SharingOrganisation.class, dir, version, parsers);
        findFileAndOpenParser(Session.class, dir, version, parsers);
        findFileAndOpenParser(SessionUser.class, dir, version, parsers);
        findFileAndOpenParser(Slot.class, dir, version, parsers);
        findFileAndOpenParser(Consultation.class, dir, version, parsers);
        findFileAndOpenParser(Diary.class, dir, version, parsers);
        findFileAndOpenParser(Observation.class, dir, version, parsers);
        findFileAndOpenParser(ObservationReferral.class, dir, version, parsers);
        findFileAndOpenParser(Problem.class, dir, version, parsers);
        findFileAndOpenParser(ClinicalCode.class, dir, version, parsers);
        findFileAndOpenParser(DrugCode.class, dir, version, parsers);
        findFileAndOpenParser(DrugRecord.class, dir, version, parsers);
        findFileAndOpenParser(IssueRecord.class, dir, version, parsers);

        if (version.equals(VERSION_5_1)) {
            findFileAndOpenParser(PatientAudit.class, dir, version, parsers); //not present in EMIS test data
            findFileAndOpenParser(RegistrationAudit.class, dir, version, parsers); //not present in EMIS test data
        }
    }

    public static void findFileAndOpenParser(Class parserCls, File dir, String version, Map<Class, AbstractCsvParser> ret) throws Exception {

        Package p = parserCls.getPackage();
        String[] packages = p.getName().split("\\.");
        String domain = packages[packages.length-1];
        String name = parserCls.getSimpleName();

        for (File f: dir.listFiles()) {
            String fName = f.getName();

            //we're only interested in CSV files
            String extension = Files.getFileExtension(fName);
            if (!extension.equalsIgnoreCase("csv")) {
                continue;
            }

            String[] toks = fName.split("_");
            if (toks.length != 5) {
                continue;
            }

            if (!toks[1].equalsIgnoreCase(domain)
                    || !toks[2].equalsIgnoreCase(name)) {
                continue;
            }

            //now construct an instance of the parser for the file we've found
            Constructor<AbstractCsvParser> constructor = parserCls.getConstructor(String.class, File.class);
            AbstractCsvParser parser = constructor.newInstance(version, f);

            ret.put(parserCls, parser);
            return;
        }

        throw new FileNotFoundException("Failed to find CSV file for " + domain + "_" + name + " in " + dir);
    }

    /**
     * validates that all expected files can be found in the folder
     */
/*    private static Map<Class, File> validateExpectedFiles(File dir, String version) throws FileNotFoundException {

        Map<Class, File> ret = new HashMap<>();
        
        findFileForParser(Location.class, dir, ret);
        findFileForParser(Organisation.class, dir, ret);
        findFileForParser(OrganisationLocation.class, dir, ret);
        findFileForParser(Patient.class, dir, ret);
        findFileForParser(UserInRole.class, dir, ret);
        findFileForParser(SharingOrganisation.class, dir, ret);
        findFileForParser(Session.class, dir, ret);
        findFileForParser(SessionUser.class, dir, ret);
        findFileForParser(Slot.class, dir, ret);
        findFileForParser(Consultation.class, dir, ret);
        findFileForParser(Diary.class, dir, ret);
        findFileForParser(Observation.class, dir, ret);
        findFileForParser(ObservationReferral.class, dir, ret);
        findFileForParser(Problem.class, dir, ret);
        findFileForParser(ClinicalCode.class, dir, ret);
        findFileForParser(DrugCode.class, dir, ret);
        findFileForParser(DrugRecord.class, dir, ret);
        findFileForParser(IssueRecord.class, dir, ret);

        if (version.equals(VERSION_5_1)) {
            findFileForParser(PatientAudit.class, dir, ret); //not present in EMIS test data
            findFileForParser(RegistrationAudit.class, dir, ret); //not present in EMIS test data
        }
        
        return ret;
    }

    public static void findFileForParser(Class parserCls, File dir,  Map<Class, File> ret) throws FileNotFoundException {

        Package p = parserCls.getPackage();
        String[] packages = p.getName().split("\\.");
        String domain = packages[packages.length-1];
        String name = parserCls.getSimpleName();

        for (File f: dir.listFiles()) {
            String fName = f.getName();

            //we're only interested in CSV files
            String extension = Files.getFileExtension(fName);
            if (!extension.equalsIgnoreCase("csv")) {
                continue;
            }

            String[] toks = fName.split("_");
            if (toks.length != 5) {
                continue;
            }

            if (!toks[1].equalsIgnoreCase(domain)
                    || !toks[2].equalsIgnoreCase(name)) {
                continue;
            }

            ret.put(parserCls, f);
            return;
        }

        throw new FileNotFoundException("Failed to find CSV file for " + domain + "_" + name + " in " + dir);
    }*/

    /*public static File getFileByPartialName(String domain, String name, File dir) throws FileNotFoundException {

        for (File f: dir.listFiles()) {
            String fName = f.getName();

            //we're only interested in CSV files
            String extension = Files.getFileExtension(fName);
            if (!extension.equalsIgnoreCase("csv")) {
                continue;
            }

            String[] toks = fName.split("_");
            if (toks.length != 5) {
                continue;
            }

            if (!toks[1].equalsIgnoreCase(domain)
                || !toks[2].equalsIgnoreCase(name)) {
                continue;
            }

            return f;
        }

        throw new FileNotFoundException("Failed to find CSV file for " + domain + "_" + name + " in " + dir);
    }*/


    private static void transformProcessingIdFolder(String version, Map<Class, AbstractCsvParser> parsers, CsvProcessor csvProcessor) throws Exception {

        EmisCsvHelper csvHelper = new EmisCsvHelper();

        //these three transforms don't create resources themselves, but cache data that the subsequent ones rely on
        ClinicalCodeTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DrugCodeTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ObservationPreTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DrugRecordPreTransformer.transform(version, parsers, csvProcessor, csvHelper);
        IssueRecordPreTransformer.transform(version, parsers, csvProcessor, csvHelper);

        //we re-use some of the parsers after this point, so reset any that need it
        for (AbstractCsvParser parser: parsers.values()) {
            parser.reset();
        }

        //run the transforms for non-patient resources
        OrganisationLocationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        LocationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        OrganisationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        UserInRoleTransformer.transform(version, parsers, csvProcessor, csvHelper);
        SessionUserTransformer.transform(version, parsers, csvProcessor, csvHelper);
        SessionTransformer.transform(version, parsers, csvProcessor, csvHelper);

        //note the order of these transforms is important, as consultations should be before obs etc.
        PatientTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ConsultationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        IssueRecordTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DrugRecordTransformer.transform(version, parsers, csvProcessor, csvHelper);
        SlotTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DiaryTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ObservationReferralTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ProblemTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ObservationTransformer.transform(version, parsers, csvProcessor, csvHelper);

        //if we have any new Obs, Conditions, Medication etc. that reference pre-existing parent obs or problems,
        //then we need to retrieve the existing resources and update them
        csvHelper.processRemainingObservationParentChildLinks(csvProcessor);

        //if we have any new Obs etc. that refer to pre-existing problems, we need to update the existing FHIR problem
        csvHelper.processRemainingProblemRelationships(csvProcessor);

        //if we have any changes to the staff in pre-existing sessions, we need to update the existing FHIR Schedules
        csvHelper.processRemainingSessionPractitioners(csvProcessor);
    }


}
