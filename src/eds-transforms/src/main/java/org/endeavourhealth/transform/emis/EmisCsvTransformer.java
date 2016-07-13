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
import org.hl7.fhir.instance.model.CodeableConcept;

import java.util.Map;

public abstract class EmisCsvTransformer {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static void transform(String folderPath, CsvProcessor csvProcessor) throws Exception {

        //always parse coding databases into maps
        Map<Long, CodeableConcept> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSV_FORMAT);
        Map<Long, CodeableConcept> fhirMedication = DrugCodeTransformer.transform(folderPath, CSV_FORMAT);

        EmisCsvHelper csvHelper = new EmisCsvHelper(clinicalCodes, fhirMedication);

        transformAdminData(folderPath, csvProcessor, csvHelper);
        transformPatientData(folderPath, csvProcessor, csvHelper);

        //tell the processor we've completed all the files, so we can now start passing work to the protocols queue
        csvProcessor.processingCompleted();
    }

    private static void transformAdminData(String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        LocationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        OrganisationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        UserInRoleTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        SessionTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }

    private static void transformPatientData(String folderPath, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        //note the order of these transforms is important, as consultations should be before obs etc.
        PatientTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        ConsultationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        //TODO - decide order for Obs, Probs and Referrals
        ObservationTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        ProblemTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);

        ObservationReferralTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        SlotTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        DiaryTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        DrugRecordTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
        IssueRecordTransformer.transform(folderPath, CSV_FORMAT, csvProcessor, csvHelper);
    }



}
