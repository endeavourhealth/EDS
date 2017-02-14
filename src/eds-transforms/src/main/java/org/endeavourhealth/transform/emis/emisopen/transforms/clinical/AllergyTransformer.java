package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AllergyListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AllergyType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public final class AllergyTransformer {


    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        AllergyListType allergyList = medicalRecord.getAllergyList();
        if (allergyList == null) {
            return;
        }

        for (AllergyType allergy : allergyList.getAllergy()) {
            Resource resource = transform(allergy, patientGuid);
            if (resource != null) {
                resources.add(resource);
            }
        }
    }

    public static AllergyIntolerance transform(EventType eventType, String patientGuid) throws TransformException
    {
        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        EmisOpenHelper.setUniqueId(fhirAllergy, patientGuid, eventType.getGUID());

        fhirAllergy.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        fhirAllergy.setRecorder(EmisOpenHelper.createPractitionerReference(eventType.getOriginalAuthor().getUser().getGUID()));

        fhirAllergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        // need to determine whether substance should be looked up via SNOMED causitive agent

        fhirAllergy.setSubstance(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirAllergy.setNote(AnnotationHelper.createAnnotation(text));
        }

        return fhirAllergy;
    }

    public static AllergyIntolerance transform(AllergyType allergy, String patientGuid) throws TransformException
    {
        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        EmisOpenHelper.setUniqueId(fhirAllergy, patientGuid, allergy.getGUID());

        fhirAllergy.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        fhirAllergy.setRecorder(EmisOpenHelper.createPractitionerReference(allergy.getOriginalAuthor().getUser().getGUID()));

        fhirAllergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(allergy.getAssignedDate(), allergy.getAssignedTime(), allergy.getDatePart()));

        // need to determine whether substance should be looked up via SNOMED causitive agent
        fhirAllergy.setSubstance(CodeConverter.convert(allergy.getCode(), allergy.getDisplayTerm()));

        String text = allergy.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirAllergy.setNote(AnnotationHelper.createAnnotation(text));
        }

        //TODO - populate remining fields from source?
        return fhirAllergy;
    }
}
