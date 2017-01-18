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

    public static final String VERSION_5_4 = "5.4"; //version being received live from Emis as of Dec 2016
    public static final String VERSION_5_3 = "5.3"; //version being received live from Emis as of Nov 2016
    public static final String VERSION_5_1 = "5.1"; //version received in official emis test pack
    public static final String VERSION_5_0 = "5.0"; //assumed version received prior to emis test pack (not sure of actual version number)

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data is different
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static void transform(UUID exchangeId, String exchangeBody, UUID serviceId, UUID systemId,
                                       TransformError transformError, List<UUID> batchIds, TransformError previousErrors,
                                       String sharedStoragePath, int maxFilingThreads) throws Exception {

        //for EMIS CSV, the exchange body will be a list of files received
        String[] files = exchangeBody.split(java.lang.System.lineSeparator());

        LOG.info("Invoking EMIS CSV transformer for {} files using {} threads", files.length, maxFilingThreads);

        //the files should all be in a directory structure of org folder -> processing ID folder -> CSV files
        File orgDirectory = validateAndFindCommonDirectory(sharedStoragePath, files);

        //we ignore the version already set in the exchange header, as Emis change versions without any notification,
        //so we dynamically work out the version when we load the first set of files
        String version = null;
        //validateVersion(version);

        //the processor is responsible for saving FHIR resources
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId, transformError, batchIds, maxFilingThreads);

        Map<Class, List<AbstractCsvParser>> allParsers = new HashMap<>();

        try {

            List<File> processingIdDirectories = getProcessingIdDirectoriesInOrder(orgDirectory);
            for (int i=0; i<processingIdDirectories.size(); i++) {
                File processingIdDirectory = processingIdDirectories.get(i);

                if (version == null) {
                    version = determineVersion(processingIdDirectory);
                }

                List<AbstractCsvParser> parsers = new ArrayList<>();

                //validate the files and, if this the first batch, open the parsers to validate the file contents (columns)
                boolean openParsers = i==0;
                validateAndOpenParsers(processingIdDirectory, version, openParsers, parsers);

                //add the parsers to the larger map, keyed on class
                for (AbstractCsvParser parser: parsers) {
                    Class cls = parser.getClass();
                    List list = allParsers.get(cls);
                    if (list == null) {
                        list = new ArrayList<>();
                        allParsers.put(cls, list);
                    }
                    list.add(parser);
                }
            }

            LOG.trace("Transforming EMIS CSV content in {}", orgDirectory);
            transformParsers(version, allParsers, processor, previousErrors, maxFilingThreads);

        } finally {

            closeParsers(allParsers);
        }

        LOG.trace("Completed transform for service {} - waiting for resources to commit to DB", serviceId);
        processor.waitToFinish();
    }

    private static void closeParsers(Map<Class, List<AbstractCsvParser>> allParsers) {

        for (Class cls: allParsers.keySet()) {
            List<AbstractCsvParser> parsers = allParsers.get(cls);
            closeParsers(parsers);
        }
    }

    private static void closeParsers(List<AbstractCsvParser> parsers) {
        for (AbstractCsvParser parser : parsers) {
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


    /**
     * the Emis schema changes without notice, so rather than define the version in the SFTP reader,
     * we simply look at the files to work out what version it really is
     */
    private static String determineVersion(File dir) throws Exception {

        String[] versions = new String[]{VERSION_5_0, VERSION_5_1, VERSION_5_3, VERSION_5_4};
        Exception lastException = null;

        for (String version: versions) {

            List<AbstractCsvParser> parsers = new ArrayList<>();
            try {
                validateAndOpenParsers(dir, version, true, parsers);

                //if we make it here, this version is the right one
                return version;

            } catch (Exception ex) {
                //ignore any exceptions, as they just mean the version is wrong, so try the next one
                lastException = ex;

            } finally {
                //make sure to close any parsers that we opened
                closeParsers(parsers);
            }
        }

        throw new TransformException("Unable to determine version for EMIS CSV", lastException);
    }

    /*private static void validateVersion(String version) throws Exception {

        if (!version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_0)
                && !version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_1)
                && !version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_3)
                && !version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_4)) {
            throw new TransformException("Unsupported version for EMIS CSV: " + version);
        }
    }*/

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


    private static void validateAndOpenParsers(File dir, String version, boolean openParser, List<AbstractCsvParser> parsers) throws Exception {

        findFileAndOpenParser(Location.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Organisation.class, dir, version, openParser, parsers);
        findFileAndOpenParser(OrganisationLocation.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Patient.class, dir, version, openParser, parsers);
        findFileAndOpenParser(UserInRole.class, dir, version, openParser, parsers);
        findFileAndOpenParser(SharingOrganisation.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Session.class, dir, version, openParser, parsers);
        findFileAndOpenParser(SessionUser.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Slot.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Consultation.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Diary.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Observation.class, dir, version, openParser, parsers);
        findFileAndOpenParser(ObservationReferral.class, dir, version, openParser, parsers);
        findFileAndOpenParser(Problem.class, dir, version, openParser, parsers);
        findFileAndOpenParser(ClinicalCode.class, dir, version, openParser, parsers);
        findFileAndOpenParser(DrugCode.class, dir, version, openParser, parsers);
        findFileAndOpenParser(DrugRecord.class, dir, version, openParser, parsers);
        findFileAndOpenParser(IssueRecord.class, dir, version, openParser, parsers);

        //these last two files aren't present in older versions
        if (version.equals(VERSION_5_3)
                || version.equals(VERSION_5_4)) {
            findFileAndOpenParser(PatientAudit.class, dir, version, openParser, parsers);
            findFileAndOpenParser(RegistrationAudit.class, dir, version, openParser, parsers);
        }

        //then validate there are no extra, unexpected files in the folder, which would imply new data
        //Set<File> sh = new HashSet<>(parsers);

        Set<File> expectedFiles = parsers
                .stream()
                .map(T -> T.getFile())
                .collect(Collectors.toSet());

        for (File file: dir.listFiles()) {
            if (file.isFile()
                    && !expectedFiles.contains(file)
                    && !Files.getFileExtension(file.getAbsolutePath()).equalsIgnoreCase("gpg")) {

                throw new FileFormatException(file, "Unexpected file " + file + " in EMIS CSV extract");
            }
        }
    }

    public static void findFileAndOpenParser(Class parserCls, File dir, String version, boolean openParser, List<AbstractCsvParser> ret) throws Exception {

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
            Constructor<AbstractCsvParser> constructor = parserCls.getConstructor(String.class, File.class, Boolean.TYPE);
            AbstractCsvParser parser = constructor.newInstance(version, f, openParser);

            ret.add(parser);
            return;
        }

        throw new FileNotFoundException("Failed to find CSV file for " + domain + "_" + name + " in " + dir);
    }


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
                                        TransformError previousErrors,
                                         int maxFilingThreads) throws Exception {

        EmisCsvHelper csvHelper = new EmisCsvHelper(findDataSharingAgreementGuid(parsers));

        //if this is the first extract for this organisation, we need to apply all the content of the admin resource cache
        if (!new AuditRepository().isServiceStarted(csvProcessor.getServiceId(), csvProcessor.getSystemId())) {
            LOG.trace("Applying admin resource cache for service {} and system {}", csvProcessor.getServiceId(), csvProcessor.getSystemId());
            csvHelper.applyAdminResourceCache(csvProcessor);
        }

        //these transforms don't create resources themselves, but cache data that the subsequent ones rely on
        ClinicalCodeTransformer.transform(version, parsers, csvProcessor, csvHelper, maxFilingThreads);
        DrugCodeTransformer.transform(version, parsers, csvProcessor, csvHelper, maxFilingThreads);
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

        //if we have any new Obs etc. that refer to pre-existing problems, we need to update the existing FHIR Problem
        csvHelper.processRemainingProblemRelationships(csvProcessor);

        //if we have any changes to the staff in pre-existing sessions, we need to update the existing FHIR Schedules
        csvHelper.processRemainingSessionPractitioners(csvProcessor);

        //process any changes to ethnicity or marital status, without a change to the Patient
        csvHelper.processRemainingEthnicitiesAndMartialStatuses(csvProcessor);

        //process any changes to Org-Location links without a change to the Location itself
        csvHelper.processRemainingOrganisationLocationMappings(csvProcessor);

        //process any changes to Problems that didn't have an associated Observation change too
        csvHelper.processRemainingProblems(csvProcessor);
    }




    public static void findRecordsToProcess(Map<Class, List<AbstractCsvParser>> allParsers, TransformError previousErrors) throws Exception {

        for (Class cls: allParsers.keySet()) {
            List<AbstractCsvParser> parsers = allParsers.get(cls);
            for (AbstractCsvParser parser: parsers) {

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
        if (TransformErrorUtility.containsArgument(previousErrors, TransformErrorUtility.ARG_FATAL_ERROR)) {
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

            String errorProcessingId = TransformErrorUtility.findArgumentValue(error, TransformErrorUtility.ARG_EMIS_CSV_DIRECTORY);
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
