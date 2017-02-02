package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ResourceType;

final class ImmunisationTransformer
{
    public static Immunization transform(EventType eventType, String patientUuid) throws TransformException
    {
        Immunization immunization = new Immunization();
        immunization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        immunization.setId(eventType.getGUID());

        immunization.setPatient(ReferenceHelper.createReference(ResourceType.Patient, patientUuid));
        immunization.setPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, eventType.getOriginalAuthor().getUser().getGUID()));

        immunization.setDateElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        immunization.setVaccineCode(CodeConverter.convert(eventType.getCode(), eventType.getDescriptiveText()));

        //todo qualifiers

        return immunization;
    }
}
