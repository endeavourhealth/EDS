package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.emis.csv.transforms.admin.LocationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.OrganisationTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.admin.UserInRoleTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SessionTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.appointment.SlotTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.careRecord.*;
import org.endeavourhealth.transform.emis.csv.transforms.coding.*;
import org.endeavourhealth.transform.emis.csv.transforms.admin.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.DrugRecordTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.prescribing.IssueRecordTransformer;
import org.endeavourhealth.transform.fhir.FhirPatientStore;
import org.endeavourhealth.transform.terminology.Snomed;
import org.hl7.fhir.instance.model.*;

import javax.swing.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public abstract class EmisCsvTransformer {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "hh:mm:ss";
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    public static Map<String, FhirPatientStore> transform(String folderPath) throws Exception {

        FhirObjectStore fhirObjects = transformMetadata(folderPath);

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

    /**
     * test harness
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {

        }

        for (int i=0; i<10; i++) {
            String term = Snomed.getTerm(22298006, 37443015);
            System.out.println("Term= " + term);
        }


      /*  javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.showDialog(null, "Open");

        File f = fc.getSelectedFile();

        try {
            Map<String, Medication> fhirMedication = DrugCodeTransformer.transform(f.getAbsolutePath(), CSV_FORMAT);

            for (Iterator<Map.Entry<String, Medication>> it = fhirMedication.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Medication> entry = it.next();
                System.out.println("Medication = " + entry.getKey() + " at " + entry.getValue());
            }

            *//*


            CSVFormat fmt = CSVFormat.DEFAULT;
                    //.withSkipHeaderRecord(true);

            CSVParser csvReader = CSVParser.parse(f, Charset.defaultCharset(), fmt);

            Map<String, Integer> header = csvReader.getHeaderMap();
            for (Iterator<Map.Entry<String, Integer>> it = header.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Integer> entry = it.next();
                System.out.println("Header = " + entry.getKey() + " at " + entry.getValue());
            }

            Iterator<CSVRecord> it = csvReader.iterator();
            while (it.hasNext()) {
                CSVRecord record = it.next();
                System.out.println("record: " + record.get(0));
            }*//*
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    private static FhirObjectStore transformMetadata(String folderPath) throws Exception {

        //parse coding files into cache maps
        Map<Long, ClinicalCode> clinicalCodes = ClinicalCodeTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Medication> fhirMedication = DrugCodeTransformer.transform(folderPath, CSV_FORMAT);

        //parse the organisational metadata
        Map<String, Location> fhirLocations = LocationTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Organization> fhirOrganisations = OrganisationTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Practitioner> fhirPractitioners = UserInRoleTransformer.transform(folderPath, CSV_FORMAT);
        Map<String, Schedule> fhirSchedules = SessionTransformer.transform(folderPath, CSV_FORMAT);

        return new FhirObjectStore(clinicalCodes, fhirMedication, fhirLocations, fhirOrganisations, fhirPractitioners, fhirSchedules);
    }




}
