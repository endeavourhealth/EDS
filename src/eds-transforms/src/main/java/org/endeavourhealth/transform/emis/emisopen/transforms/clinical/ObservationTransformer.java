package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.SimpleQuantity;

import java.math.BigDecimal;

final class ObservationTransformer
{
    public static Observation transform(EventType eventType, String patientUuid) throws TransformException
    {
        Observation observation = new Observation();
        observation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        observation.setId(eventType.getGUID());

        observation.setSubject(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        observation.addPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, eventType.getOriginalAuthor().getUser().getGUID()));

        observation.setEffective(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        observation.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDescriptiveText()));

        if (eventType.getNumericValue() != null)
        {
            SimpleQuantity simpleQuantity = new SimpleQuantity();
            simpleQuantity.setValue(BigDecimal.valueOf(eventType.getNumericValue().getValue()));
            simpleQuantity.setUnit(eventType.getNumericValue().getUnits());
            observation.setValue(simpleQuantity);
        }

        return observation;
    }
}
