package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CsvMetadata;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.ConsultationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCodeTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.EventTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.PrescriptionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.coding.DrugCodeTransformer;
import org.endeavourhealth.transform.emis.emisopen.transforms.admin.ScheduleTransformer;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public abstract class EmisCsvTransformer {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    public static String TIME_FORMAT = "hh:mm:ss";

    public static Map<String, List<Resource>> transform(String folderPath) throws Exception {

        //parse coding files into cache maps (coding domain)
        Map<Long, Object> clinicalCodes = ClinicalCodeTransformer.transformClinicalCodes(folderPath, CSVFormat.DEFAULT);
        Map<Long, Object> drugCodes = DrugCodeTransformer.transformDrugCodes(folderPath, CSVFormat.DEFAULT);

        //parse the non-patient metadata
        Map<UUID, Location> fhirLocations = LocationTransformer.transformLocations(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Organization> fhirOrganisations = OrganisationTransformer.transformOrganisations(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Practitioner> fhirPractitioners = UserInRoleTransformer.transformUsersInRole(folderPath, CSVFormat.DEFAULT);
        Map<UUID, Schedule> fhirSchedules = SessionTransformer.transformSessions(folderPath, CSVFormat.DEFAULT);

        //now start parsing the patient data
        Map<String, List<Resource>> fhirResources = new HashMap<>();

        PatientTransformer.transformPatients(folderPath, CSVFormat.DEFAULT, fhirResources);

        SlotTransformer.transformSlots(folderPath, CSVFormat.DEFAULT, fhirResources);

        ConsultationTransformer.transformConsultations(folderPath, CSVFormat.DEFAULT, fhirResources);


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
