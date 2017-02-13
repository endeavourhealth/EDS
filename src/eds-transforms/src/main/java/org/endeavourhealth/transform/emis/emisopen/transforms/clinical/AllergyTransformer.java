package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;

final class AllergyTransformer
{
    public static AllergyIntolerance transform(EventType eventType, String patientGuid) throws TransformException
    {
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        EmisOpenHelper.setUniqueId(allergy, patientGuid, eventType.getGUID());

        allergy.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        allergy.setRecorder(EmisOpenHelper.createPractitionerReference(eventType.getOriginalAuthor().getUser().getGUID()));

        allergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        // need to determine whether substance should be looked up via SNOMED causitive agent

        allergy.setSubstance(CodeConverter.convert(eventType.getCode(), eventType.getDescriptiveText()));

        return allergy;
    }
}
