package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ResourceType;

final class AllergyTransformer
{
    public static AllergyIntolerance transform(EventType eventType, String patientUuid) throws TransformException
    {
        AllergyIntolerance allergy = new AllergyIntolerance();
        allergy.setId(eventType.getGUID());
        allergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        allergy.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        allergy.setRecorder(ReferenceHelper.createReference(ResourceType.Practitioner, eventType.getOriginalAuthor().getUser().getGUID()));

        allergy.setOnsetElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        return allergy;
    }
}
