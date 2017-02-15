package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IdentType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;

final class ImmunisationTransformer extends ClinicalTransformerBase {

    public static void transform(EventType eventType, List<Resource> results, String patientGuid) throws TransformException
    {
        Immunization fhirImmunization = new Immunization();
        fhirImmunization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        EmisOpenHelper.setUniqueId(fhirImmunization, patientGuid, eventType.getGUID());

        fhirImmunization.setPatient(EmisOpenHelper.createPatientReference(patientGuid));

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            fhirImmunization.setPerformer(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        fhirImmunization.setDateElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        fhirImmunization.setVaccineCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirImmunization.addNote(AnnotationHelper.createAnnotation(text));
        }

        //TODO - qualifiers

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirImmunization, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirImmunization, recordedByGuid);

        results.add(fhirImmunization);
    }
}
