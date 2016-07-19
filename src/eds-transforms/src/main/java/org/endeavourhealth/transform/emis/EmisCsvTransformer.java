package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
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
import org.endeavourhealth.transform.terminology.Snomed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EmisCsvTransformer.class);
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data has different
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static void transform(String folderPath, CsvProcessor csvProcessor) throws Exception {

        EmisCsvHelper csvHelper = new EmisCsvHelper();

        transformCodes(folderPath, csvProcessor, csvHelper);
        transformAdminData(folderPath, csvProcessor, csvHelper);
        transformPatientData(folderPath, csvProcessor, csvHelper);

        //tell the processor we've completed all the files, so we can now start passing work to the protocols queue
        csvProcessor.processingCompleted();
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



}
