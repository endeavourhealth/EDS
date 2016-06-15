package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;

final class ObservationTransformer
{
    public static Observation transform(EventType eventType, String patientUuid) throws TransformException
    {
        Observation observation = new Observation();
        observation.setId(eventType.getGUID());
        observation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        observation.setSubject(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        observation.addPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, eventType.getOriginalAuthor().getUser().getGUID()));

        observation.setEffective(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        return observation;
    }
}
