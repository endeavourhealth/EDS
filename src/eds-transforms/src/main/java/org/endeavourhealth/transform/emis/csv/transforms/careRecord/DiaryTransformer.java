package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Diary;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ProcedureRequest;

import java.util.Date;

public class DiaryTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, EmisCsvHelper objectStore) throws Exception {

        CareRecord_Diary parser = new CareRecord_Diary(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProcedureRequest(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProcedureRequest(CareRecord_Diary diaryParser, EmisCsvHelper objectStore) throws Exception {

        ProcedureRequest fhirRequest = new ProcedureRequest();
        fhirRequest.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE_REQUEST));

        String diaryGuid = diaryParser.getDiaryGuid();
        String patientGuid = diaryParser.getPatientGuid();
        String organisationGuid = diaryParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirRequest, patientGuid, diaryGuid);

        fhirRequest.setSubject(objectStore.createPatientReference(patientGuid));

        boolean store = !diaryParser.getDeleted() && !diaryParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirRequest, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        CodeableConcept fhirConcept = objectStore.findClinicalCode(diaryParser.getCodeId()).createCodeableConcept();
        fhirRequest.setCode(fhirConcept);

        String originalTerm = diaryParser.getOriginalTerm();
        if (!Strings.isNullOrEmpty(originalTerm)) {
            fhirConcept.setText(originalTerm);
        }

        Date effectiveDate = diaryParser.getEffectiveDate();
        if (effectiveDate != null) {
            String effectiveDatePrecision = diaryParser.getEffectiveDatePrecision();
            fhirRequest.setScheduled(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));
        } else {
            String freeTextDuration = diaryParser.getDurationTerm();
            if (!Strings.isNullOrEmpty(freeTextDuration)) {
                //TODO - need somewhere to store Diary DurationTerm in ProcedureRequest resource
            }
        }

        String clinicianGuid = diaryParser.getClinicianUserInRoleGuid();
        if (clinicianGuid != null) {
            fhirRequest.setPerformer(objectStore.createPractitionerReference(clinicianGuid, patientGuid));
        }

        String associatedText = diaryParser.getAssociatedText();
        fhirRequest.addNotes(AnnotationHelper.createAnnotation(associatedText));

        Date entererdDateTime = diaryParser.getEnteredDateTime();
        if (entererdDateTime != null) {
            fhirRequest.setOrderedOn(entererdDateTime);
        }

        String enterdByGuid = diaryParser.getEnteredByUserInRoleGuid();
        if (enterdByGuid != null) {
            fhirRequest.setOrderer(objectStore.createPractitionerReference(enterdByGuid, patientGuid));
        }

        String consultationGuid = diaryParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirRequest.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        if (diaryParser.getIsComplete()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.COMPLETED);
        } else if (diaryParser.getIsActive()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.REQUESTED);
        } else {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.SUSPENDED);
        }

        //TODO - need somewhere to store Diary LocationTypeDescription in FHIR ProcedureRequest resource
        //String locationDescription = diaryParser.getLocationTypeDescription();
    }
}
