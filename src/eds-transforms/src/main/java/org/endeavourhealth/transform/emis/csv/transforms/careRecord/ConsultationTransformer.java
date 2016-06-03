package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.Admin_Patient;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Consultation;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConsultationTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Map<String, List<Resource>> fhirResources) throws Exception {

        CareRecord_Consultation parser = new CareRecord_Consultation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createEncounter(parser, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createEncounter(CareRecord_Consultation consultationParser, Map<String, List<Resource>> fhirResources) throws Exception {

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

        UUID consultationGuid = consultationParser.getConsultationGuid();
        fhirEncounter.setId(consultationGuid.toString());

        UUID patientGuid = consultationParser.getPatientGuid();
        EmisCsvTransformer.addToMap(patientGuid, fhirEncounter, fhirResources);

        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        fhirEncounter.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientGuid.toString()));

        UUID appointmentGuid = consultationParser.getAppointmentSlotGuid();
        if (appointmentGuid != null) {
            fhirEncounter.setAppointment(ReferenceHelper.createReference(ResourceType.Appointment, appointmentGuid.toString()));
        }

        UUID clinicianUuid = consultationParser.getClinicianUserInRoleGuid();
        if (clinicianUuid != null) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.setIndividual(ReferenceHelper.createReference(ResourceType.Practitioner, clinicianUuid.toString()));
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