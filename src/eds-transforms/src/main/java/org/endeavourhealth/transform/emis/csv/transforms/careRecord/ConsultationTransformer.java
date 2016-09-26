package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.PeriodHelper;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class ConsultationTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Consultation parser = new Consultation(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createEncounter(parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createEncounter(Consultation consultationParser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Encounter fhirEncounter = new Encounter();
        fhirEncounter.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        String consultationGuid = consultationParser.getConsultationGuid();
        String patientGuid = consultationParser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirEncounter, patientGuid, consultationGuid);

        fhirEncounter.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (consultationParser.getDeleted() || consultationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirEncounter);
            return;
        }

        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        String appointmentGuid = consultationParser.getAppointmentSlotGuid();
        if (!Strings.isNullOrEmpty(appointmentGuid)) {
            fhirEncounter.setAppointment(csvHelper.createAppointmentReference(appointmentGuid, patientGuid));
        }

        String clinicianUuid = consultationParser.getClinicianUserInRoleGuid();
        if (!Strings.isNullOrEmpty(clinicianUuid)) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept(EncounterParticipantType.PRIMARY_PERFORMER));
            fhirParticipant.setIndividual(csvHelper.createPractitionerReference(clinicianUuid));
        }

        //the test files do not contain a column for the entered
        String enteredByUuid = consultationParser.getEnteredByUserInRoleGuid();
        Date enteredDateTime = consultationParser.getEnteredDateTime();
        if (!Strings.isNullOrEmpty(enteredByUuid)
                && enteredDateTime != null) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept("Entered By"));

            if (!Strings.isNullOrEmpty(enteredByUuid)) {
                fhirParticipant.setIndividual(csvHelper.createPractitionerReference(enteredByUuid));
            }

            if (enteredDateTime != null) {
                fhirParticipant.setPeriod(PeriodHelper.createPeriod(enteredDateTime, null));
            }
        }

        Date date = consultationParser.getEffectiveDate();
        String precision = consultationParser.getEffectiveDatePrecision();
        Period fhirPeriod = createPeriod(date, precision);
        if (fhirPeriod != null) {
            fhirEncounter.setPeriod(fhirPeriod);
        }

        Long codeId = consultationParser.getConsultationSourceCodeId();
        if (codeId != null) {
            CodeableConcept fhirCode = csvHelper.findClinicalCode(codeId, csvProcessor);
            fhirEncounter.addReason(fhirCode);
        }

        csvProcessor.savePatientResource(patientGuid, fhirEncounter);
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