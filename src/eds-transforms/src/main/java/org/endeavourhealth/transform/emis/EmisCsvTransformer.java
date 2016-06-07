package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ConsultationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ObservationReferralTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ObservationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ProblemTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.*;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordTransformer;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public abstract class EmisCsvTransformer {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    public static String TIME_FORMAT = "hh:mm:ss";

    public static Map<String, List<Resource>> transform(String folderPath) throws Exception {

        FhirObjectStore fhirObjects = transformMetadata(folderPath);

        PatientTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        ProblemTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        SlotTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        ConsultationTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        ObservationTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        ObservationReferralTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        //TODO - DiaryTransformer
        DrugRecordTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);
        IssueRecordTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirObjects);

        return fhirObjects.getFhirPatientResources();
    }

    private static FhirObjectStore transformMetadata(String folderPath) throws Exception {

        //parse coding files into cache maps
        Map<Long, ClinicalCode> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Medication> fhirMedication = DrugCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);

        //parse the organisational metadata
        Map<String, Location> fhirLocations = LocationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Organization> fhirOrganisations = OrganisationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Practitioner> fhirPractitioners = UserInRoleTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Schedule> fhirSchedules = SessionTransformer.transform(folderPath, CSVFormat.DEFAULT);

        return new FhirObjectStore(clinicalCodes, fhirMedication, fhirLocations, fhirOrganisations, fhirPractitioners, fhirSchedules);
    }




}
