package org.endeavourhealth.transform.emis;

import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.common.exceptions.UnexpectedOrganisationException;
import org.endeavourhealth.transform.emis.csv.EmisCsvFileSplitter;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Organisation;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.*;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.DrugCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordTransformer;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class EmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformer.class);

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data has different
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static void transform(File folder, CsvProcessor csvProcessor) throws Exception {
        transform(folder.getAbsolutePath(), csvProcessor);
    }
    public static void transform(String folderPath, CsvProcessor csvProcessor) throws Exception {

        LOG.trace("Transforming EMIS CSV content in " + folderPath);

        EmisCsvHelper csvHelper = new EmisCsvHelper();

        transformCodes(folderPath, csvProcessor, csvHelper);
        transformAdminData(folderPath, csvProcessor, csvHelper);
        transformPatientData(folderPath, csvProcessor, csvHelper);

        LOG.trace("Completed EMIS CSV transform in " + folderPath);
    }

    private static void transformCodes(String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        LOG.trace("ClinicalCodeTransformer");
        ClinicalCodeTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("DrugCodeTransformer");
        DrugCodeTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }

    private static void transformAdminData(String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        LOG.trace("LocationTransformer");
        LocationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("OrganisationTransformer");
        OrganisationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("UserInRoleTransformer");
        UserInRoleTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("SessionTransformer");
        SessionTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }

    private static void transformPatientData(String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        //invoke any pre-transformers, which extract referential data from the files before the main transforms
        LOG.trace("ObservationPreTransformer");
        ObservationPreTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        //note the order of these transforms is important, as consultations should be before obs etc.
        LOG.trace("PatientTransformer");
        PatientTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("ConsultationTransformer");
        ConsultationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("ObservationReferralTransformer");
        ObservationReferralTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("ProblemTransformer");
        ProblemTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("ObservationTransformer");
        ObservationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("IssueRecordTransformer");
        IssueRecordTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("DrugRecordTransformer");
        DrugRecordTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("SlotTransformer");
        SlotTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        LOG.trace("DiaryTransformer");
        DiaryTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        //if we have any new Obs that reference pre-existing parent obs or problems,
        //then we need to retrieve the existing resources and update them
        csvHelper.processRemainingObservationParentChildLinks(csvProcessor);
        csvHelper.processRemainingProblemRelationships(csvProcessor);
    }

    public static List<UUID> splitAndTransform(String[] files,
                                               UUID exchangeId,
                                               UUID serviceId,
                                               UUID systemId,
                                               Set<UUID> orgIds) throws Exception {

        LOG.info("Invoking EMIS CSV transformer for " + files.length + " files");

        //validate that all the files are in the same directory
        File commonDir = validateCommonDirectory(files);

        //validate that we've got all the files we expect
        List<File> expectedFiles = validateExpectedFiles(commonDir);

        //validate there's no additional files in the common directory
        validateNoExtraFiles(commonDir, expectedFiles);

        //split the source files by Organisation GUID
        File srcDir = commonDir;
        File dstDir = new File(srcDir, "Split");
        EmisCsvFileSplitter.splitFiles(srcDir, dstDir);

        //the sub-directories will be named by the org GUID, so validate we've only got orgs we expect
        validateOrganisations(serviceId, systemId, dstDir, orgIds);

        final List<UUID> batchIds = Collections.synchronizedList(new ArrayList<>());

        //process the non-organisation files on their own, before anything else
        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId);
        EmisCsvTransformer.transform(dstDir, processor);
        batchIds.addAll(processor.getBatchIdsCreated());

        //having done the non-organisation data, we can do the organisation files in parallel
        ExecutorService pool = Executors.newFixedThreadPool(5); //arbitrarily chosen five threads

        for (File splitDir: dstDir.listFiles()) {
            if (splitDir.isDirectory()) {

                pool.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        CsvProcessor processor = new CsvProcessor(exchangeId, serviceId, systemId);
                        EmisCsvTransformer.transform(splitDir, processor);
                        batchIds.addAll(processor.getBatchIdsCreated());
                        return null;
                    }
                });
            }
        }

        //wait for all pools to complete
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);

        //having successfully processed all the split files, delete the split content (if any exceptions were raised, this won't happen)
        dstDir.delete();

        LOG.info("Returning from EMIS CSV transformer for " + files.length + " files");

        return batchIds;
    }

    private static void validateOrganisations(UUID serviceId, UUID systemId, File folder, Set<UUID> orgIds) throws Exception {

        //retrieve the Organisations from the org repository, and extract their ODS codes
        Set<Organisation> orgs = new OrganisationRepository().getByUds(orgIds);
        Set<String> orgOdsCodes = orgs
                                    .stream()
                                    .map(t -> t.getNationalId())
                                    .collect(Collectors.toSet());

        for (File f: folder.listFiles()) {
            if (!f.isDirectory()) {
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

        Admin_Organisation parser = new Admin_Organisation(folder.getAbsolutePath(), CSV_FORMAT);
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
                && !fileSet.contains(file)) {
                throw new FileFormatException(file, "Unexpected file " + file + " in EMIS CSV extract");
            }
        }
    }

    /**
     * validates that all expected files can be found in the folder
     */
    private static List<File> validateExpectedFiles(File dir) throws FileNotFoundException {

        List<File> list = new ArrayList<>();
        list.add(getFileByPartialName("Admin_Location", dir));
        list.add(getFileByPartialName("Admin_Organisation", dir));
        list.add(getFileByPartialName("Admin_OrganisationLocation", dir));
        list.add(getFileByPartialName("Admin_Patient", dir));
        list.add(getFileByPartialName("Admin_UserInRole", dir));
        list.add(getFileByPartialName("Agreements_SharingOrganisation", dir));
        list.add(getFileByPartialName("Appointment_Session", dir));
        list.add(getFileByPartialName("Appointment_SessionUser", dir));
        list.add(getFileByPartialName("Appointment_Slot", dir));
        list.add(getFileByPartialName("Audit_PatientAudit", dir));
        list.add(getFileByPartialName("Audit_RegistrationAudit", dir));
        list.add(getFileByPartialName("CareRecord_Consultation", dir));
        list.add(getFileByPartialName("CareRecord_Diary", dir));
        list.add(getFileByPartialName("CareRecord_Observation", dir));
        list.add(getFileByPartialName("CareRecord_ObservationReferral", dir));
        list.add(getFileByPartialName("CareRecord_Problem", dir));
        list.add(getFileByPartialName("Coding_ClinicalCode", dir));
        list.add(getFileByPartialName("Coding_DrugCode", dir));
        list.add(getFileByPartialName("Prescribing_DrugRecord", dir));
        list.add(getFileByPartialName("Prescribing_IssueRecord", dir));
        return list;
    }

    public static File getFileByPartialName(String partialFileName, File dir) throws FileNotFoundException {

        //append a trailing underscore, so we don't pick up CareRecord_ObservationReferral when looking for CareRecord_Observation
        partialFileName += "_";

        for (File f: dir.listFiles()) {
            String name = f.getName();
            String extension = Files.getFileExtension(name);
            if (!extension.equalsIgnoreCase("csv")) {
                continue;
            }

            if (name.indexOf(partialFileName) == -1) {
                continue;
            }

            return f;
        }

        throw new FileNotFoundException("Failed to find CSV file for " + partialFileName);
    }
}
