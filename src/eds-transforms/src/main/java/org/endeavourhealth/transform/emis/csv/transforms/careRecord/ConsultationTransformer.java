package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Consultation;
import org.endeavourhealth.transform.emis.openhr.schema.VocDatePart;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.Map;

public class ConsultationTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Consultation.class);
        while (parser.nextRecord()) {

            try {
                createResource((Consultation)parser, fhirResourceFiler, csvHelper, version);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }

    private static void createResource(Consultation parser,
                                        FhirResourceFiler fhirResourceFiler,
                                        EmisCsvHelper csvHelper,
                                        String version) throws Exception {

        Encounter fhirEncounter = new Encounter();
        fhirEncounter.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        String consultationGuid = parser.getConsultationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirEncounter, patientGuid, consultationGuid);

        fhirEncounter.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirEncounter);
            return;
        }

        fhirEncounter.setStatus(Encounter.EncounterState.FINISHED);

        String appointmentGuid = parser.getAppointmentSlotGuid();
        if (!Strings.isNullOrEmpty(appointmentGuid)) {
            fhirEncounter.setAppointment(csvHelper.createAppointmentReference(appointmentGuid, patientGuid));
        }

        String clinicianUuid = parser.getClinicianUserInRoleGuid();
        if (!Strings.isNullOrEmpty(clinicianUuid)) {
            Encounter.EncounterParticipantComponent fhirParticipant = fhirEncounter.addParticipant();
            fhirParticipant.addType(CodeableConceptHelper.createCodeableConcept(EncounterParticipantType.PRIMARY_PERFORMER));
            fhirParticipant.setIndividual(csvHelper.createPractitionerReference(clinicianUuid));
        }

        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (!Strings.isNullOrEmpty(enteredByGuid)) {
            Reference reference = csvHelper.createPractitionerReference(enteredByGuid);
            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
        }

        //in the earliest version of the extract, we only got the entered date and not time
        Date enteredDateTime = null;
        if (version.equals(EmisCsvTransformer.VERSION_5_0)) {
            enteredDateTime = parser.getEnteredDate();
        } else {
            enteredDateTime = parser.getEnteredDateTime();
        }

        if (enteredDateTime != null) {
            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
        }

        Date date = parser.getEffectiveDate();
        String precision = parser.getEffectiveDatePrecision();
        Period fhirPeriod = createPeriod(date, precision);
        if (fhirPeriod != null) {
            fhirEncounter.setPeriod(fhirPeriod);
        }

        String organisationGuid = parser.getOrganisationGuid();
        fhirEncounter.setServiceProvider(csvHelper.createOrganisationReference(organisationGuid));

        Long codeId = parser.getConsultationSourceCodeId();
        String term = parser.getConsultationSourceTerm();
        if (codeId != null || !Strings.isNullOrEmpty(term)) {

            CodeableConcept fhirCodeableConcept = null;
            if (codeId != null) {
                fhirCodeableConcept = csvHelper.findClinicalCode(codeId);
            }
            if (!Strings.isNullOrEmpty(term)) {
                if (fhirCodeableConcept == null) {
                    fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(term);
                } else {
                    fhirCodeableConcept.setText(term);
                }
            }

            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ENCOUNTER_SOURCE, fhirCodeableConcept));
        }

        //since complete consultations are by far the default, only record the incomplete extension if it's not complete
        if (!parser.getComplete()) {
            BooleanType b = new BooleanType(false);
            fhirEncounter.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ENCOUNTER_INCOMPLETE, b));
        }

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirEncounter);
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