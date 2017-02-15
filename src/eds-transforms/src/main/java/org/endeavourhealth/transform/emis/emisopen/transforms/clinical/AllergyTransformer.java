package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;

public final class AllergyTransformer extends ClinicalTransformerBase {


    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        AllergyListType allergyList = medicalRecord.getAllergyList();
        if (allergyList == null) {
            return;
        }

        for (AllergyType allergy : allergyList.getAllergy()) {
            transform(allergy, resources, patientGuid);
        }
    }

    public static void transform(EventType eventType, List<Resource> resources, String patientGuid) throws TransformException {

        AllergyIntolerance fhirAllergy = createBasicAllergy(eventType, patientGuid);

        //TODO - populate remaining fields from source

        resources.add(fhirAllergy);
    }

    public static void transform(AllergyType allergy, List<Resource> resources, String patientGuid) throws TransformException {

        AllergyIntolerance fhirAllergy = createBasicAllergy(allergy, patientGuid);

        //TODO - populate remaining fields from source?

        resources.add(fhirAllergy);
    }

    private static AllergyIntolerance createBasicAllergy(CodedItemBaseType codedItem, String patientGuid) throws TransformException {

        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        EmisOpenHelper.setUniqueId(fhirAllergy, patientGuid, codedItem.getGUID());

        fhirAllergy.setPatient(EmisOpenHelper.createPatientReference(patientGuid));

        IdentType author = codedItem.getAuthorID();
        if (author != null) {
            fhirAllergy.setRecorder(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        fhirAllergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(codedItem.getAssignedDate(), codedItem.getAssignedTime(), codedItem.getDatePart()));

        // need to determine whether substance should be looked up via SNOMED causitive agent
        fhirAllergy.setSubstance(CodeConverter.convert(codedItem.getCode(), codedItem.getDisplayTerm()));

        String text = codedItem.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirAllergy.setNote(AnnotationHelper.createAnnotation(text));
        }

        Date dateRecorded = findRecordedDate(codedItem.getOriginalAuthor());
        fhirAllergy.setRecordedDate(dateRecorded);

        String recordedByGuid = findRecordedUserGuid(codedItem.getOriginalAuthor());
        addRecordedByExtension(fhirAllergy, recordedByGuid);

        return fhirAllergy;
    }
}
