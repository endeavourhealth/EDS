package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.DiaryListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.DiaryType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IdentType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.common.fhir.AnnotationHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.StringType;

import java.util.List;

public class DiaryTransformer {


    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        DiaryListType diaryList = medicalRecord.getDiaryList();
        if (diaryList == null) {
            return;
        }

        for (DiaryType diaryEntry : diaryList.getDiary()) {
            transform(diaryEntry, resources, patientGuid);
        }
    }

    public static void transform(DiaryType diaryEntry, List<Resource> resources, String patientGuid) throws TransformException {

        ProcedureRequest fhirRequest = new ProcedureRequest();
        fhirRequest.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE_REQUEST));

        String diaryGuid = diaryEntry.getGUID();
        EmisCsvHelper.setUniqueId(fhirRequest, patientGuid, diaryGuid);

        fhirRequest.setSubject(EmisOpenHelper.createPatientReference(patientGuid));

        fhirRequest.setScheduled(DateConverter.convertPartialDateToDateTimeType(diaryEntry.getAssignedDate(), diaryEntry.getAssignedTime(), diaryEntry.getDatePart()));

        fhirRequest.setCode(CodeConverter.convert(diaryEntry.getCode(), diaryEntry.getDisplayTerm()));

        String text = diaryEntry.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirRequest.addNotes(AnnotationHelper.createAnnotation(text));
        }

        String duration = diaryEntry.getDurationTerm();
        if (!Strings.isNullOrEmpty(duration)) {
            fhirRequest.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURE_REQUEST_SCHEDULE_TEXT, new StringType(duration)));
        }

        IdentType author = diaryEntry.getAuthorID();
        if (author != null) {
            fhirRequest.setOrderer(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        //TODO - finish

        /**
         protected BigInteger templateID;
         protected BigInteger templateInstanceID;
         protected String templateComponentName;
         protected String reminder;
         protected String reminderType;
         protected IdentType locationTypeID;
         */

        resources.add(fhirRequest);

/*

        String originalTerm = parser.getOriginalTerm();
        if (!Strings.isNullOrEmpty(originalTerm)) {
            CodeableConcept fhirConcept = fhirRequest.getCode();
            fhirConcept.setText(originalTerm);
        }

        //handle mis-spelt column in EMIS test pack
        //String clinicianGuid = diaryParser.getClinicianUserInRoleGuid();
        String clinicianGuid = null;
        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_0)
                || version.equals(EmisCsvToFhirTransformer.VERSION_5_1)) {
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
*/

    }
}
