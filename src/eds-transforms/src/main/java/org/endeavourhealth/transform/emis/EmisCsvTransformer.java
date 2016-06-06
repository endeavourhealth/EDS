package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ConsultationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.DiaryTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ObservationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.*;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public abstract class EmisCsvTransformer {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    public static String TIME_FORMAT = "hh:mm:ss";

    public static Map<String, List<Resource>> transform(String folderPath) throws Exception {

        Metadata metadata = transformMetadata(folderPath);

        //now start parsing the patient data
        Map<String, List<Resource>> fhirResources = new HashMap<>();

        PatientTransformer.transform(folderPath, CSVFormat.DEFAULT, metadata, fhirResources);
        SlotTransformer.transform(folderPath, CSVFormat.DEFAULT, metadata, fhirResources);
        ConsultationTransformer.transform(folderPath, CSVFormat.DEFAULT, metadata, fhirResources);
        ObservationTransformer.transform(folderPath, CSVFormat.DEFAULT, metadata, fhirResources);
        //DiaryTransformer
        //ObservationReferral
        //Problem
        //DrugRecord
        //IssueRecord

        //TODO - need to copy Organisations and Locations into patient resource lists


        return fhirResources;
    }

    private static Metadata transformMetadata(String folderPath) throws Exception {

        //parse coding files into cache maps
        Map<Long, ClinicalCode> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<Long, DrugCode> drugCodes = DrugCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);

        //parse the organisational metadata
        Map<String, Location> fhirLocations = LocationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Organization> fhirOrganisations = OrganisationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Practitioner> fhirPractitioners = UserInRoleTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<String, Schedule> fhirSchedules = SessionTransformer.transform(folderPath, CSVFormat.DEFAULT);

        return new Metadata(clinicalCodes, drugCodes, fhirLocations, fhirOrganisations, fhirPractitioners, fhirSchedules);
    }




}
