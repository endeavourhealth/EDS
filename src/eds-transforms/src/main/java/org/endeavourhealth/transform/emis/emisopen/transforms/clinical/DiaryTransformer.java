package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.AnnotationHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.DiaryListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.DiaryType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IdentType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.StringType;

import java.util.Date;
import java.util.List;

public class DiaryTransformer extends ClinicalTransformerBase {


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

        Date dateRecorded = findRecordedDate(diaryEntry.getOriginalAuthor());
        if (dateRecorded != null) {
            fhirRequest.setOrderedOn(dateRecorded);
        }

        String recordedByGuid = findRecordedUserGuid(diaryEntry.getOriginalAuthor());
        if (!Strings.isNullOrEmpty(recordedByGuid)) {
            fhirRequest.setOrderer(EmisOpenHelper.createPractitionerReference(recordedByGuid));
        }

        IdentType author = diaryEntry.getAuthorID();
        if (author != null) {
            fhirRequest.setPerformer(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        //NOTE: the EmisOpen XML doesn't contain an indicator on the STATUS of a Diary Entry, so we can't set this in the resource
        /*if (parser.getIsComplete()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.COMPLETED);
        } else if (parser.getIsActive()) {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.REQUESTED);
        } else {
            fhirRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.SUSPENDED);
        }*/

        linkToProblem(diaryEntry, patientGuid, fhirRequest, resources);

        resources.add(fhirRequest);
    }
}
