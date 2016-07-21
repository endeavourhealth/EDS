package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Consultation;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class ConsultationTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        CareRecord_Consultation parser = new CareRecord_Consultation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createEncounter(parser, csvProcessor, csvHelper);
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createEncounter(CareRecord_Consultation consultationParser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Encounter fhirEncounter = new Encounter();
        fhirEncounter.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        String consultationGuid = consultationParser.getConsultationGuid();
        String patientGuid = consultationParser.getPatientGuid();
        String organisationGuid = consultationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirEncounter, patientGuid, consultationGuid);

        fhirEncounter.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (consultationParser.getDeleted() || consultationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirEncounter, patientGuid);
            return;
        }

        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        String appointmentGuid = consultationParser.getAppointmentSlotGuid();
        if (appointmentGuid != null) {
            fhirEncounter.setAppointment(csvHelper.createAppointmentReference(appointmentGuid, patientGuid));
        }

        String clinicianUuid = consultationParser.getClinicianUserInRoleGuid();
        if (clinicianUuid != null) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.setIndividual(csvHelper.createPractitionerReference(clinicianUuid));
        }

        Date date = consultationParser.getEffectiveDate();
        String precision = consultationParser.getEffectiveDatePrecision();
        Period fhirPeriod = createPeriod(date, precision);
        if (fhirPeriod != null) {
            fhirEncounter.setPeriod(fhirPeriod);
        }

        csvProcessor.savePatientResource(fhirEncounter, patientGuid);
    }

    private static Period createPeriod(Date date, String precision) throws Exception {
        if (date == null) {
            return null;
        }

        VocDatePart vocPrecision = VocDatePart.fromValue(precision);
        if (vocPrecision == null) {
            throw new IllegalArgumentException("Unsupported consultation precision [" + precision + "]");
        }

        Period fhirPeriod = new Period();
        switch (vocPrecision) {
            case U:
                return null;
            case Y:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.YEAR));
                break;
            case YM:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.MONTH));
                break;
            case YMD:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.DAY));
                break;
            case YMDT:
                fhirPeriod.setStartElement(new DateTimeType(date, TemporalPrecisionEnum.SECOND));
                break;
            default:
                throw new IllegalArgumentException("Unexpected date precision " + vocPrecision);
        }
        return fhirPeriod;
    }
}