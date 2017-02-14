package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Meta;

final class ImmunisationTransformer
{
    public static Immunization transform(EventType eventType, String patientGuid) throws TransformException
    {
        Immunization immunization = new Immunization();
        immunization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        EmisOpenHelper.setUniqueId(immunization, patientGuid, eventType.getGUID());

        immunization.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        immunization.setPerformer(EmisOpenHelper.createPractitionerReference(eventType.getOriginalAuthor().getUser().getGUID()));

        immunization.setDateElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        immunization.setVaccineCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            immunization.addNote(AnnotationHelper.createAnnotation(text));
        }

        //todo qualifiers

        return immunization;
    }
}
