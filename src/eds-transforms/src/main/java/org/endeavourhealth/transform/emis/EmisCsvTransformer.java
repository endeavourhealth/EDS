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
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.DrugCodeTransformer;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public abstract class EmisCsvTransformer {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    public static String TIME_FORMAT = "hh:mm:ss";

    public static Map<String, List<Resource>> transform(String folderPath) throws Exception {

        //parse coding files into cache maps (coding domain)
        Map<Long, Object> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<Long, Object> drugCodes = DrugCodeTransformer.transform(folderPath, CSVFormat.DEFAULT);

        //parse the non-patient metadata
        Map<UUID, Location> fhirLocations = LocationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Organization> fhirOrganisations = OrganisationTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Practitioner> fhirPractitioners = UserInRoleTransformer.transform(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Schedule> fhirSchedules = SessionTransformer.transform(folderPath, CSVFormat.DEFAULT);

        //now start parsing the patient data
        Map<String, List<Resource>> fhirResources = new HashMap<>();

        PatientTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirResources);
        SlotTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirResources);
        ConsultationTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirResources);
        ObservationTransformer.transform(folderPath, CSVFormat.DEFAULT, fhirResources);
        //DiaryTransformer
        //ObservationReferral
        //Problem
        //DrugRecord
        //IssueRecord

        //TODO - need to copy Organisations and Locations into patient resource lists


        return fhirResources;
    }

    public static void addToMap(UUID patientGuid, Resource fhirResource, Map<String, List<Resource>> hmResources) {
        List<Resource> l = hmResources.get(patientGuid.toString());
        if (l == null) {
            l = new ArrayList<Resource>();
            hmResources.put(patientGuid.toString(), l);
        }
        l.add(fhirResource);
    }



}
