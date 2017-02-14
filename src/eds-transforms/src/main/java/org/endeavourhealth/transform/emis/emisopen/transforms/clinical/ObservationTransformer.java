package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.SimpleQuantity;

import java.math.BigDecimal;

final class ObservationTransformer
{
    public static Observation transform(EventType eventType, String patientGuid) throws TransformException {

        Observation observation = new Observation();
        observation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        EmisOpenHelper.setUniqueId(observation, patientGuid, eventType.getGUID());

        observation.setSubject(EmisOpenHelper.createPatientReference(patientGuid));
        observation.addPerformer(EmisOpenHelper.createPractitionerReference(eventType.getOriginalAuthor().getUser().getGUID()));

        observation.setEffective(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        observation.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        if (eventType.getNumericValue() != null)
        {
            SimpleQuantity simpleQuantity = new SimpleQuantity();
            simpleQuantity.setValue(BigDecimal.valueOf(eventType.getNumericValue().getValue()));
            simpleQuantity.setUnit(eventType.getNumericValue().getUnits());
            observation.setValue(simpleQuantity);
        }

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            observation.setComments(text);
        }

        return observation;
    }
}
