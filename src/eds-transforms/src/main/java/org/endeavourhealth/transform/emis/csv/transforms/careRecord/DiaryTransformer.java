package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Diary;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.StringType;

import java.util.Date;
import java.util.Map;

public class DiaryTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Diary parser = (Diary)parsers.get(Diary.class);

        while (parser.nextRecord()) {

            try {
                createResource(version, parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createResource(String version,
                                       Diary parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        ProcedureRequest fhirRequest = new ProcedureRequest();
        fhirRequest.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE_REQUEST));

        String diaryGuid = parser.getDiaryGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirRequest, patientGuid, diaryGuid);

        fhirRequest.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(parser.getCurrentState(), patientGuid, fhirRequest);
            return;
        }

        Long codeId = parser.getCodeId();
        fhirRequest.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String originalTerm = parser.getOriginalTerm();
        if (!Strings.isNullOrEmpty(originalTerm)) {
            CodeableConcept fhirConcept = fhirRequest.getCode();
            fhirConcept.setText(originalTerm);
        }

        Date effectiveDate = parser.getEffectiveDate();
        if (effectiveDate != null) {
            String effectiveDatePrecision = parser.getEffectiveDatePrecision();
            fhirRequest.setScheduled(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));
        }

        String freeTextDuration = parser.getDurationTerm();
        if (!Strings.isNullOrEmpty(freeTextDuration)) {
            fhirRequest.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURE_REQUEST_SCHEDULE_TEXT, new StringType(freeTextDuration)));
        }

        //handle mis-spelt column in EMIS test pack
        //String clinicianGuid = diaryParser.getClinicianUserInRoleGuid();
        String clinicianGuid = null;
        if (version.equals(EmisCsvTransformer.VERSION_TEST_PACK)) {
            clinicianGuid = parser.getClinicanUserInRoleGuid();
        } else {
            clinicianGuid = parser.getClinicianUserInRoleGuid();
        }

        if (!Strings.isNullOrEmpty(clinicianGuid)) {
            fhirRequest.setPerformer(csvHelper.createPractitionerReference(clinicianGuid));
        }

        String associatedText = parser.getAssociatedText();
        fhirRequest.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String locationTypeDescription = parser.getLocationTypeDescription();
        if (!Strings.isNullOrEmpty(locationTypeDescription)) {
            fhirRequest.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURE_REQUEST_LOCATION_DESCRIPTION, new StringType(locationTypeDescription)));
        }

        Date entererdDateTime = parser.getEnteredDateTime();
        if (entererdDateTime != null) {
            fhirRequest.setOrderedOn(entererdDateTime);
        }

        String enterdByGuid = parser.getEnteredByUserInRoleGuid();
        if (!Strings.isNullOrEmpty(enterdByGuid)) {
            fhirRequest.setOrderer(csvHelper.createPractitionerReference(enterdByGuid));
        }

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirRequest.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        if (parser.getIsComplete()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.COMPLETED);
        } else if (parser.getIsActive()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.REQUESTED);
        } else {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.SUSPENDED);
        }

        csvProcessor.savePatientResource(parser.getCurrentState(), patientGuid, fhirRequest);
    }
}
