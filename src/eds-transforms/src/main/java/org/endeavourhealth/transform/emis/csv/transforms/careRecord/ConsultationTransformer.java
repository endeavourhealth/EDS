package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Consultation;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConsultationTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        CareRecord_Consultation parser = new CareRecord_Consultation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createEncounter(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createEncounter(CareRecord_Consultation consultationParser, FhirObjectStore objectStore) throws Exception {

        //ignore deleted consultations
        if (consultationParser.getDeleted()) {
            return;
        }

        //confidential information shouldn't be stored in EDS and we're not handling deltas
        if (consultationParser.isConfidential()) {
            return;
        }

        Encounter fhirEncounter = new Encounter();
        fhirEncounter.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        String consultationGuid = consultationParser.getConsultationGuid();
        fhirEncounter.setId(consultationGuid);

        String patientGuid = consultationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirEncounter);

        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        fhirEncounter.setPatient(objectStore.createPatientReference(patientGuid));

        String appointmentGuid = consultationParser.getAppointmentSlotGuid();
        if (appointmentGuid != null) {
            fhirEncounter.setAppointment(objectStore.createAppointmentReference(appointmentGuid, patientGuid));
        }

        String clinicianUuid = consultationParser.getClinicianUserInRoleGuid();
        if (clinicianUuid != null) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.setIndividual(objectStore.createPractitionerReference(clinicianUuid, patientGuid));
        }

        Date date = consultationParser.getEffectiveDate();
        String precision = consultationParser.getEffectiveDatePrecision();
        Period fhirPeriod = createPeriod(date, precision);
        if (fhirPeriod != null) {
            fhirEncounter.setPeriod(fhirPeriod);
        }


        //TODO - how to handle CareRecord_Consultation complete flag?
    }

    private static Period createPeriod(Date date, String precision) throws Exception {
        if (date == null) {
            return null;
        }

        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new TransformException("Unsupported consultation precision [" + precision + "]");
        }

        Period fhirPeriod = new Period();
        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.YEAR));
            case YM:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.MONTH));
            case YMD:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.DAY));
            case YMDT:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.SECOND));
        }
        return fhirPeriod;
    }
}