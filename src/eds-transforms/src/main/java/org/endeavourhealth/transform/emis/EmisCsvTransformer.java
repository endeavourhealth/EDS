package org.endeavourhealth.transform.emis;

import com.google.common.io.Files;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.admin.*;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Session;
import org.endeavourhealth.transform.emis.csv.schema.appointment.SessionUser;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Slot;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.*;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.coding.DrugCode;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public abstract class EmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformer.class);

    public static final String VERSION_5_1 = "5.1";
    public static final String VERSION_TEST_PACK = "TestPack";

    public static List<UUID> transform(String version,
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
            List<File> expectedFiles = validateExpectedFiles(processingIdDirectory, version);

            //validate there's no additional files in the common directory
            LOG.trace("Validating no additional files in {}", processingIdDirectory);
            validateNoExtraFiles(processingIdDirectory, expectedFiles);

            //validate the column names match what we expect (this is repeated when we actually perform
            //the transform, but by doing it now, we fail earlier rather than later)
            LOG.trace("Validating CSV column names in {}", processingIdDirectory);
            validateColumnNames(processingIdDirectory, version);
        }

        //the orgDirectory contains a sub-directory for each processing ID, which must be processed in order
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

        LOG.trace("Starting transform for organisation {}", orgDirectory.getName());

        for (Integer processingId: processingIds) {
            File file = hmFiles.get(processingId);
            EmisCsvTransformerWorker.transform(version, file, processor);
        }

        LOG.trace("Completed transfom for organisation {} - waiting for resources to commit to DB", orgDirectory.getName());

        return processor.getBatchIdsCreated();
    }

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

    /*static class TransformOrganisation implements Callable {

        private String version = null;
        private File orgDirectory = null;
        private CsvProcessor processor = null;

        public TransformOrganisation(String version, File orgDirectory, CsvProcessor processor) {
            this.version = version;
            this.orgDirectory = orgDirectory;
            this.processor = processor;
        }

        @Override
        public Object call() throws Exception {

            LOG.trace("Starting transfom for organisation {}", orgDirectory.getName());

            //the directory will contain a sub-directory for each processing ID, which must be processed in order
            List<Integer> processingIds = new ArrayList<>();
            Map<Integer, File> hmFiles = new HashMap<>();
            for (File file: orgDirectory.listFiles()) {
                Integer processingId = Integer.valueOf(file.getName());
                processingIds.add(processingId);
                hmFiles.put(processingId, file);
            }

            Collections.sort(processingIds);

            for (Integer processingId: processingIds) {
                File file = hmFiles.get(processingId);
                EmisCsvTransformerWorker.transform(version, file, processor);
            }

            LOG.trace("Completed transfom for organisation {}", orgDirectory.getName());

            return null;
        }
    }*/

    /*private static void validateOrganisations(String version, File folder, UUID serviceId, UUID systemId, Set<UUID> orgIds) throws Exception {

        //retrieve the Organisations from the org repository, and extract their ODS codes
        Set<org.endeavourhealth.core.data.admin.models.Organisation> orgs = new OrganisationRepository().getByUds(orgIds);
        Set<String> orgOdsCodes = orgs
                                    .stream()
                                    .map(t -> t.getNationalId())
                                    .collect(Collectors.toSet());

        //the file splitter has split into directories name using the org GUID, so we can just look at the directories
        for (File f: folder.listFiles()) {
            if (!f.isDirectory()) {
                continue;
            }

            //ignore the admin folder
            if (f.getName().equals(EmisCsvFileSplitter.ADMIN_FOLDER_NAME)) {
                continue;
            }

            String orgGuid = f.getName();
            String odsCode = null;

            //first, use the ID helper to find the EDS ID for an organisation resource with the local orgGUID
            UUID edsOrgId = IdHelper.getEdsResourceId(serviceId, systemId, ResourceType.Organization, orgGuid);
            if (edsOrgId != null) {
                //retrieve from EHR
                odsCode = findOrganisationOdsFromEhrRepository(edsOrgId);
            }

            if (odsCode == null) {
                //if there's no EDS org ID it means we've never encountered this organisation before, in which case we need
                //to look in the Admin_Organisation file we're received, to find the ODS code for that organisation
                File adminFolder = new File(folder, EmisCsvFileSplitter.ADMIN_FOLDER_NAME);
                for (File adminBatchFolder: adminFolder.listFiles()) {

                    odsCode = findOrganisationOdsFromCsvFile(version, orgGuid, adminBatchFolder);
                    if (odsCode != null) {
                        break;
                    }
                }

            }

            if (odsCode == null
                    || !orgOdsCodes.contains(odsCode)) {
                throw new UnexpectedOrganisationException(orgGuid, odsCode);
            }
        }
    }

    private static String findOrganisationOdsFromCsvFile(String version, String orgGuid, File folder) throws Exception {

        Organisation parser = new Organisation(version, folder.getAbsolutePath(), EmisCsvTransformerWorker.CSV_FORMAT);
        try {
            while (parser.nextRecord()) {
                String csvOrgGuid = parser.getOrganisationGuid();
                if (csvOrgGuid.equalsIgnoreCase(orgGuid)) {
                    return parser.getODScode();
                }
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

        return null;
    }

    private static String findOrganisationOdsFromEhrRepository(UUID edsOrgId) throws Exception {
        ResourceHistory resourceHistory = new ResourceRepository().getCurrentVersion(ResourceType.Organization.toString(), edsOrgId);
        if (resourceHistory == null) {
            return null;
        }

        Organization fhirOrganisation = (Organization)new JsonParser().parse(resourceHistory.getResourceData());
        for (Identifier fhirIdentifier: fhirOrganisation.getIdentifier()) {
            if (fhirIdentifier.getSystem().equalsIgnoreCase(FhirUri.IDENTIFIER_SYSTEM_ODS_CODE)) {
                return fhirIdentifier.getValue();
            }
        }

        return null;
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

    private static void validateColumnNames(File commonDir, String version) throws Exception {
        String folderPath = commonDir.getAbsolutePath();

        //the column validation is performed in the constructor of each file parser, so just create and immediately close
        new Location(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Organisation(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new OrganisationLocation(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Patient(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new UserInRole(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Session(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new SessionUser(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Slot(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Consultation(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Diary(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Observation(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new ObservationReferral(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new Problem(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new ClinicalCode(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new DrugCode(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new DrugRecord(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
        new IssueRecord(version, folderPath, EmisCsvTransformerWorker.CSV_FORMAT).close();
    }

    /**
     * validates that the directory contains ONLY the given files
     */
    private static void validateNoExtraFiles(File commonDir, List<File> files) throws Exception {

        Set<File> fileSet = new HashSet<>(files);

        for (File file: commonDir.listFiles()) {
            if (file.isFile()
                && !fileSet.contains(file)
                && !Files.getFileExtension(file.getAbsolutePath()).equalsIgnoreCase("gpg")) {

                throw new FileFormatException(file, "Unexpected file " + file + " in EMIS CSV extract");
            }
        }
    }

    /**
     * validates that all expected files can be found in the folder
     */
    private static List<File> validateExpectedFiles(File dir, String version) throws FileNotFoundException {

        List<File> list = new ArrayList<>();
        list.add(getFileByPartialName("Admin", "Location", dir));
        list.add(getFileByPartialName("Admin", "Organisation", dir));
        list.add(getFileByPartialName("Admin", "OrganisationLocation", dir));
        list.add(getFileByPartialName("Admin", "Patient", dir));
        list.add(getFileByPartialName("Admin", "UserInRole", dir));
        list.add(getFileByPartialName("Agreements", "SharingOrganisation", dir));
        list.add(getFileByPartialName("Appointment", "Session", dir));
        list.add(getFileByPartialName("Appointment", "SessionUser", dir));
        list.add(getFileByPartialName("Appointment", "Slot", dir));
        list.add(getFileByPartialName("CareRecord", "Consultation", dir));
        list.add(getFileByPartialName("CareRecord", "Diary", dir));
        list.add(getFileByPartialName("CareRecord", "Observation", dir));
        list.add(getFileByPartialName("CareRecord", "ObservationReferral", dir));
        list.add(getFileByPartialName("CareRecord", "Problem", dir));
        list.add(getFileByPartialName("Coding", "ClinicalCode", dir));
        list.add(getFileByPartialName("Coding", "DrugCode", dir));
        list.add(getFileByPartialName("Prescribing", "DrugRecord", dir));
        list.add(getFileByPartialName("Prescribing", "IssueRecord", dir));

        if (version.equals(VERSION_5_1)) {
            list.add(getFileByPartialName("Audit", "PatientAudit", dir)); //not present in EMIS test data
            list.add(getFileByPartialName("Audit", "RegistrationAudit", dir)); //not present in EMIS test data
        }
        return list;
    }

    public static File getFileByPartialName(String domain, String name, File dir) throws FileNotFoundException {

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
    }
}
