package org.endeavourhealth.transform.emis;

import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.TransformError;
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
                                       TransformError transformError,
                                       TransformError previousErrors,
                                       int maxFilingThreads) throws Exception {

        LOG.info("Invoking EMIS CSV transformer for {} files using {} threads", files.length, maxFilingThreads);

        //validate the version
        validateVersion(version);

        //the files should all be in a directory structure of org folder -> processing ID folder -> CSV files
        File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);

        //the processor is responsible for saving FHIR resources
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId, transformError, maxFilingThreads);

        Map<Class, List<AbstractCsvParser>> allParsers = new HashMap<>();

        try {

            List<File> processingIdDirectories = getProcessingIdDirectoriesInOrder(orgDirectory);
            for (int i = 0; i < processingIdDirectories.size(); i++) {
                File processingIdDirectory = processingIdDirectories.get(i);

                Map<Class, AbstractCsvParser> parsers = new HashMap<>();

                //validate that we've got all the files we expect
                LOG.trace("Validating all files are present in {}", processingIdDirectory);
                validateAndOpenParsers(processingIdDirectory, version, parsers);

                //validate there's no additional files in the common directory
                LOG.trace("Validating no additional files in {}", processingIdDirectory);
                validateNoExtraFiles(processingIdDirectory, parsers);

                for (Class cls : parsers.keySet()) {
                    AbstractCsvParser parser = parsers.get(cls);
                    List list = allParsers.get(cls);
                    if (list == null) {
                        list = new ArrayList<>();
                        allParsers.put(cls, list);
                    }
                    list.add(parser);
                }
            }

            LOG.trace("Transforming EMIS CSV content in {}", orgDirectory);
            transformParsers(version, allParsers, processor, previousErrors);

        } finally {

            closeParsers(allParsers);
        }

        LOG.trace("Completed transform for organisation {} - waiting for resources to commit to DB", orgDirectory.getName());
        return processor.getBatchIdsCreated();
    }
    /*public static List<UUID> transform(String version,
                                       String sharedStoragePath,
                                       String[] files,
                                       UUID exchangeId,
                                       UUID serviceId,
                                       UUID systemId,
                                       ExchangeTransformAudit transformAudit,
                                       TransformError previousErrors) throws Exception {

        LOG.info("Invoking EMIS CSV transformer for {} files", files.length);

        //validate the version
        validateVersion(version);

        //the files should all be in a directory structure of org folder -> processing ID folder -> CSV files
        File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);

        //the processor is responsible for saving FHIR resources
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId, transformAudit);

        List<File> processingIdDirectories = getProcessingIdDirectoriesInOrder(orgDirectory);
        for (int i=0; i<processingIdDirectories.size(); i++) {
            File processingIdDirectory = processingIdDirectories.get(i);
            String processingIdStr = processingIdDirectory.getName();

            if (shouldProcessProcessingId(processingIdDirectory.getName(), previousErrors)) {

                Map<Class, AbstractCsvParser> parsers = new HashMap<>();

                try {
                    //validate that we've got all the files we expect
                    LOG.trace("Validating all files are present in {}", processingIdDirectory);
                    validateAndOpenParsers(processingIdDirectory, version, parsers);

                    //validate there's no additional files in the common directory
                    LOG.trace("Validating no additional files in {}", processingIdDirectory);
                    validateNoExtraFiles(processingIdDirectory, parsers);

                    LOG.trace("Transforming EMIS CSV content in {}", processingIdDirectory);
                    transformProcessingIdFolder(version, parsers, processor, processingIdStr, previousErrors);

                } catch (Exception ex) {

                    logProcessingIdError(transformAudit, ex, processingIdStr);

                    //if we've had an error at this level, something is fundamentally wrong with the files we've
                    //received, and we shouldn't continue to process any remaining processing ID folders, so log an
                    //error with them and break out
                    for (int j=i+1; j<processingIdDirectories.size(); j++) {
                        File remainingProcessingIdDirectory = processingIdDirectories.get(j);
                        logProcessingIdError(transformAudit, null, remainingProcessingIdDirectory.getName());
                    }

                    break;

                } finally {

                    //we need to update any record-level errors from this transform to indicate the processing ID they came from
                    updateRecordErrors(transformAudit, processingIdStr);

                    closeParsers(parsers);
                }
            }
        }

        LOG.trace("Completed transform for organisation {} - waiting for resources to commit to DB", orgDirectory.getName());
        return processor.getBatchIdsCreated();
    }*/

    /**
     * when we have a record-level error, we know the file and row number, but not the processing ID
     * folder, so we need to update those errors with the processing ID
     */
    /*private static void updateRecordErrors(ExchangeTransformAudit transformAudit, String processingIdStr) {

        //if there haven't been any errors, this'll be null
        if (transformAudit.getErrorXml() == null) {
            return;
        }

        TransformError container = null;
        try {
            container = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());
        } catch (Exception xmlException) {
            LOG.error("Error parsing XML " + transformAudit.getErrorXml(), xmlException);
        }

        boolean changed = false;

        for (Error error: container.getError()) {

            //for each error that doesn't contain a processing ID argument, add one
            if (!TransformErrorUtility.containsArgument(error, TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID)) {

                Arg arg = new Arg();
                arg.setName(TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID);
                arg.setValue(processingIdStr);
                error.getArg().add(arg);

                changed = true;
            }
        }

        //and save the updated record
        if (changed) {
            TransformErrorUtility.save(transformAudit, container);
        }
    }*/

    /**
     * tests if we should process the given processing ID, given any previous errors we may have had
     */
    /*private static boolean shouldProcessProcessingId(String processingIdStr, TransformError previousErrors) {

        //if this is the first time we running this
        if (previousErrors == null) {
            return true;
        }

        //if we previously had a fatal exception, then we want to run all the processing IDs
        if (TransformErrorUtility.containsArgument(previousErrors, TransformErrorUtility.ARG_FATAL_ERROR)
            || TransformErrorUtility.containsArgument(previousErrors, TransformErrorUtility.ARG_FORCE_RE_RUN)) {
            return true;
        }

        //otherwise, see if we had an error with that specific processing ID
        for (Error error: previousErrors.getError()) {

            String failedProcessingId = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID);
            if (failedProcessingId.equals(processingIdStr)) {
                return true;
            }
        }

        //if we didn't have an error with this processing ID previously, then we want to skip it
        return false;
    }*/

    /**
     * called if we get an error within a processing ID folder (e.g. can't open a file or file columns wrong)
     */
    /*public static void logProcessingIdError(ExchangeTransformAudit transformAudit, Exception ex, String processingId) {

        if (ex != null) {
            LOG.error("Error with processing ID " + processingId, ex);
        } else {
            LOG.error("Skipping processing ID " + processingId);
        }

        //then add the error to our audit object
        Map<String, String> args = new HashMap<>();
        args.put(TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID, processingId);

        TransformErrorUtility.addTransformError(transformAudit, ex, args);
    }*/

    private static void closeParsers(Map<Class, List<AbstractCsvParser>> allParsers) {

        for (Class cls: allParsers.keySet()) {
            List<AbstractCsvParser> parsers = allParsers.get(cls);
            for (AbstractCsvParser parser : parsers) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    //don't worry if this fails, as we're done anyway
                }
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


    private static String findDataSharingAgreementGuid(Map<Class, List<AbstractCsvParser>> parsers) throws Exception {

        //we need a file name to work out the data sharing agreement ID, so just the first file we can find
        File f = parsers
                .values()
                .iterator()
                .next()
                .get(0)
                .getFile();

        String name = Files.getNameWithoutExtension(f.getName());
        String[] toks = name.split("_");
        if (toks.length != 5) {
            throw new TransformException("Failed to extract data sharing agreement GUID from filename " + f.getName());
        }
        return toks[4];
    }


    private static void transformParsers(String version,
                                        Map<Class, List<AbstractCsvParser>> parsers,
                                        CsvProcessor csvProcessor,
                                        TransformError previousErrors) throws Exception {

        EmisCsvHelper csvHelper = new EmisCsvHelper(findDataSharingAgreementGuid(parsers));

        //if this is the first extract for this organisation, we need to apply all the content of the admin resource cache
        if (!new AuditRepository().isServiceStarted(csvProcessor.getServiceId(), csvProcessor.getSystemId())) {
            LOG.trace("Applying admin resource cache for service {} and system {}", csvProcessor.getServiceId(), csvProcessor.getSystemId());
            csvHelper.applyAdminResourceCache(csvProcessor);
        }

        //these transforms don't create resources themselves, but cache data that the subsequent ones rely on
        ClinicalCodeTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DrugCodeTransformer.transform(version, parsers, csvProcessor, csvHelper);
        OrganisationLocationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        SessionUserTransformer.transform(version, parsers, csvProcessor, csvHelper);
        ObservationPreTransformer.transform(version, parsers, csvProcessor, csvHelper);
        DrugRecordPreTransformer.transform(version, parsers, csvProcessor, csvHelper);
        IssueRecordPreTransformer.transform(version, parsers, csvProcessor, csvHelper);

        //before getting onto the files that actually create FHIR resources, we need to
        //work out what record numbers to process, if we're re-running a transform
        findRecordsToProcess(parsers, previousErrors);

        //run the transforms for non-patient resources
        LocationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        OrganisationTransformer.transform(version, parsers, csvProcessor, csvHelper);
        UserInRoleTransformer.transform(version, parsers, csvProcessor, csvHelper);
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

        csvHelper.processRemainingEthnicitiesAndMartialStatuses(csvProcessor);
    }




    public static void findRecordsToProcess(Map<Class, List<AbstractCsvParser>> allParsers, TransformError previousErrors) throws Exception {

        for (Class cls: allParsers.keySet()) {
            List<AbstractCsvParser> parsers = allParsers.get(cls);
            for (AbstractCsvParser parser: parsers) {

                //we've already used some of the parsers already, so reset them to the start
                parser.reset();

                String fileName = parser.getFile().getName();
                String processingIdStr = parser.getFile().getParent();

                Set<Long> recordNumbers = findRecordNumbersToProcess(fileName, processingIdStr, previousErrors);
                parser.setRecordNumbersToProcess(recordNumbers);
            }
        }
    }

    private static Set<Long> findRecordNumbersToProcess(String fileName, String processingIdStr, TransformError previousErrors) {

        //if we're running for the first time, then return null to process the full file
        if (previousErrors == null) {
            return null;
        }

        //if we previously had a fatal exception, then we want to process the full file
        if (TransformErrorUtility.containsArgument(previousErrors, TransformErrorUtility.ARG_FATAL_ERROR)
                || TransformErrorUtility.containsArgument(previousErrors, TransformErrorUtility.ARG_FORCE_RE_RUN)) {
            return null;
        }

        //if we previously had a processing ID-level error, then we want to process the full file
        /*for (Error error: previousErrors.getError()) {

            String errorProcessingId = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID);
            if (errorProcessingId.equals(processingIdStr)
                    && !TransformErrorUtility.containsArgument(error, TransformErrorUtility.ARG_EMIS_CSV_FILE)) {
                return null;
            }
        }*/

        //if we make it to here, we only want to process specific record numbers in our file, or even none, if there were
        //no previous errors processing this specific file
        HashSet<Long> recordNumbers = new HashSet<>();

        for (Error error: previousErrors.getError()) {

            String errorProcessingId = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_PROCESSING_ID);
            if (errorProcessingId.equals(processingIdStr)) {

                String errorFileName = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_FILE);
                if (errorFileName.equals(fileName)) {

                    String errorRecordNumber = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_RECORD_NUMBER);
                    recordNumbers.add(new Long(errorRecordNumber));
                }
            }
        }

        return recordNumbers;
    }
}
