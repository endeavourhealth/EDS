package org.endeavourhealth.transform.emis;

import com.google.common.io.Files;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.common.exceptions.UnexpectedOrganisationException;
import org.endeavourhealth.transform.emis.csv.EmisCsvFileSplitter;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.ThreadPool;
import org.endeavourhealth.transform.emis.csv.schema.admin.Organisation;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class EmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformer.class);

    public static List<UUID> splitAndTransform(String[] files,
                                               UUID exchangeId,
                                               UUID serviceId,
                                               UUID systemId,
                                               Set<UUID> orgIds) throws Exception {

        LOG.info("Invoking EMIS CSV transformer for {} files", files.length);

        //validate that all the files are in the same directory
        LOG.trace("Validating all files are in same directory");
        File commonDir = validateCommonDirectory(files);

        //validate that we've got all the files we expect
        LOG.trace("Validating all files are present");
        List<File> expectedFiles = validateExpectedFiles(commonDir);

        //validate there's no additional files in the common directory
        LOG.trace("Validating no additional files");
        validateNoExtraFiles(commonDir, expectedFiles);

        //split the source files by Organisation GUID
        File srcDir = commonDir;
        File dstDir = new File(srcDir, "Split");
        EmisCsvFileSplitter.splitFiles(srcDir, dstDir);

        //the sub-directories will be named by the org GUID, so validate we've only got orgs we expect
        LOG.trace("Validating no unexpected organisations");
        validateOrganisations(serviceId, systemId, dstDir, orgIds);

        //the processor is reponsible for saving FHIR resources
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId);

        //process the non-organisation files on their own, before anything else
        File adminDir = new File(dstDir, EmisCsvFileSplitter.ADMIN_FOLDER_NAME);
        new TransformOrganisation(adminDir, processor).call();

        //having done the non-organisation data, we can do the organisation files in parallel
        ThreadPool pool = new ThreadPool(5); //arbitrarily chosen five threads

        for (File orgDir: dstDir.listFiles()) {
            if (orgDir.isDirectory()
                    && !orgDir.getName().equals(EmisCsvFileSplitter.ADMIN_FOLDER_NAME)) {

                pool.submit(new TransformOrganisation(orgDir, processor));
            }
        }

        //close the pool and wait for all pools to complete
        LOG.trace("All transforms submitted - waiting for them to finish");
        pool.waitAndStop(1, TimeUnit.HOURS);

        //having successfully processed all the split files, delete the split content (if any exceptions were raised, this won't happen)
        //dstDir.delete();

        LOG.trace("All transforms completed - waiting for resources to commit to DB");
        return processor.getBatchIdsCreated();
    }

    static class TransformOrganisation implements Callable {

        private File orgDirectory = null;
        private CsvProcessor processor = null;

        public TransformOrganisation(File orgDirectory, CsvProcessor processor) {
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
                EmisCsvTransformerWorker.transform(file, processor);
            }

            LOG.trace("Completed transfom for organisation {}", orgDirectory.getName());

            return null;
        }
    }

    private static void validateOrganisations(UUID serviceId, UUID systemId, File folder, Set<UUID> orgIds) throws Exception {

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
                odsCode = findOrganisationOdsFromCsvFile(orgGuid, folder);
            }

            if (odsCode == null
                    || !orgOdsCodes.contains(odsCode)) {
                throw new UnexpectedOrganisationException(orgGuid, odsCode);
            }
        }
    }

    private static String findOrganisationOdsFromCsvFile(String orgGuid, File folder) throws Exception {

        Organisation parser = new Organisation(folder.getAbsolutePath(), EmisCsvTransformerWorker.CSV_FORMAT);
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
    }

    /**
     * validates that all the files in the array are in the same common directory
     */
    private static File validateCommonDirectory(String[] files) throws Exception {
        String commonDir = null;
        for (String file: files) {
            File f = new File(file);
            if (!f.exists()) {
                throw new FileNotFoundException("" + f + " doesn't exist");
            }
            if (commonDir == null) {
                commonDir = f.getParent();
            } else {
                if (!commonDir.equalsIgnoreCase(f.getParent())) {
                    throw new FileNotFoundException("" + f + " is not in expected directory " + commonDir);
                }
            }
        }
        return new File(commonDir);
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
    private static List<File> validateExpectedFiles(File dir) throws FileNotFoundException {

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
        list.add(getFileByPartialName("Audit", "PatientAudit", dir));
        list.add(getFileByPartialName("Audit", "RegistrationAudit", dir));
        list.add(getFileByPartialName("CareRecord", "Consultation", dir));
        list.add(getFileByPartialName("CareRecord", "Diary", dir));
        list.add(getFileByPartialName("CareRecord", "Observation", dir));
        list.add(getFileByPartialName("CareRecord", "ObservationReferral", dir));
        list.add(getFileByPartialName("CareRecord", "Problem", dir));
        list.add(getFileByPartialName("Coding", "ClinicalCode", dir));
        list.add(getFileByPartialName("Coding", "DrugCode", dir));
        list.add(getFileByPartialName("Prescribing", "DrugRecord", dir));
        list.add(getFileByPartialName("Prescribing", "IssueRecord", dir));
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

        throw new FileNotFoundException("Failed to find CSV file for " + domain + "_" + name);
    }
}
