package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
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
import org.endeavourhealth.transform.fhir.FhirPatientStore;
import org.endeavourhealth.transform.terminology.Snomed;
import org.hl7.fhir.instance.model.*;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class EmisCsvTransformer {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static List<FhirPatientStore> transform(String folderPath, UUID serviceId, UUID systemInstanceId) throws Exception {

        EmisCsvHelper fhirObjects = transformMetadata(folderPath);

        PatientTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        ProblemTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        SlotTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        ConsultationTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        ObservationTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        ObservationReferralTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        DiaryTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        DrugRecordTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);
        IssueRecordTransformer.transform(folderPath, CSV_FORMAT, fhirObjects);

        return fhirObjects.getFhirPatientStores();
    }

    private static EmisCsvHelper transformMetadata(String folderPath) throws Exception {

        //parse coding files into cache maps
        Map<Long, CodeableConcept> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSV_FORMAT);
        Map<Long, CodeableConcept> fhirMedication = DrugCodeTransformer.transform(folderPath, CSV_FORMAT);

        //parse the organisational metadata
        Map<String, Location> fhirLocations = LocationTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Organization> fhirOrganisations = OrganisationTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Practitioner> fhirPractitioners = UserInRoleTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Schedule> fhirSchedules = SessionTransformer.transform(folderPath, CSV_FORMAT);

        return new EmisCsvHelper(clinicalCodes, fhirMedication, fhirLocations, fhirOrganisations, fhirPractitioners, fhirSchedules);
    }




}
