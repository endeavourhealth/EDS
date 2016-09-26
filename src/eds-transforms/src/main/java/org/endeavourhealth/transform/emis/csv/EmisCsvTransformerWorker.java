package org.endeavourhealth.transform.emis.csv;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionUserTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.*;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.DrugCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EmisCsvTransformerWorker {
    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformerWorker.class);

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data has different
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static void transform(String version, File folder, CsvProcessor csvProcessor) throws Exception {
        transform(version, folder.getAbsolutePath(), csvProcessor);
    }
    public static void transform(String version, String folderPath, CsvProcessor csvProcessor) throws Exception {

        LOG.trace("Transforming EMIS CSV content in {}", folderPath);

        EmisCsvHelper csvHelper = new EmisCsvHelper();

        transformCodes(version, folderPath, csvProcessor, csvHelper);
        transformAdminData(version, folderPath, csvProcessor, csvHelper);
        transformPatientData(version, folderPath, csvProcessor, csvHelper);

        LOG.trace("Completed EMIS CSV transform in {}", folderPath);
    }

    private static void transformCodes(String version, String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        ClinicalCodeTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        DrugCodeTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }

    private static void transformAdminData(String version, String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        LocationTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        OrganisationTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        UserInRoleTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        SessionUserTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        SessionTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }

    private static void transformPatientData(String version, String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        //invoke any pre-transformers, which extract referential data from the files before the main transforms
        ObservationPreTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        //note the order of these transforms is important, as consultations should be before obs etc.
        PatientTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        ConsultationTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        IssueRecordTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        DrugRecordTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        SlotTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        DiaryTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        ObservationReferralTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        ProblemTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        ObservationTransformer.transform(version, folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        //if we have any new Obs, Conditions, Medication etc. that reference pre-existing parent obs or problems,
        //then we need to retrieve the existing resources and update them
        csvHelper.processRemainingObservationParentChildLinks(csvProcessor);

        //if we have any new Obs etc. that refer to pre-existing problems, we need to update the existing FHIR problem
        csvHelper.processProblemRelationships(csvProcessor);

        //if we have any changes to the staff in pre-existing sessions, we need to update the existing FHIR Schedules
        csvHelper.processRemainingSessionPractitioners(csvProcessor);
    }
}
