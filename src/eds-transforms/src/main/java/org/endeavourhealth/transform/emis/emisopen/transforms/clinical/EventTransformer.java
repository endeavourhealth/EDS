package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.AnnotationHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class EventTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EventTransformer.class);

    //Internal use 0 = text, 1= Observation; 2= Problems; 5=Values; 7=Attachments; 8=Referrals;
    //        10=Alerts; 11=Allergies; 12=Family History; 13=Immunisations; 14=Problem Ratings
    enum ObservationType
    {
        TEXT(0),
        OBSERVATION(1),
        PROBLEM(2),
        VALUE(5),
        ATTACHMENT(7),
        REFERRAL(8),
        ALERT(10),
        ALLERGY(11),
        FAMILYHISTORY(12),
        IMMUNISATION(13),
        PROBLEMRATING(14);

        private final int value;

        ObservationType(final int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return this.value;
        }

        public static ObservationType fromValue(int value)
        {
            for (ObservationType observationType : ObservationType.values())
                if (observationType.getValue() == value)
                    return observationType;

            throw new IllegalArgumentException(Integer.toString(value));
        }
    }

    public static void transform(MedicalRecordType medicalRecordType, List<Resource> results, String patientUuid) throws TransformException {

        //got records that have null event lists
        EventListType eventList = medicalRecordType.getEventList();
        if (eventList == null) {
            return;
        }

        for (EventType eventType : eventList.getEvent()) {
            Resource resource = transform(eventType, patientUuid);

            //need to handle null resources being returned
            if (resource != null) {
                results.add(resource);
            }
            //resource.add(transform(eventType, medicalRecordType.getRegistration().getGUID()));
        }
    }

    public static Resource transform(EventType eventType, String patientUuid) throws TransformException {

        //check for routing to FHIR based on the code first
        /*StringCodeType code = eventType.getCode();
        if (code != null) {
            String read2Code = code.getValue();
            if (Read2.isProcedure(read2Code)) {
                return transformProcedure(eventType, patientUuid);

            } else if (Read2.isDisorder(read2Code)) {
                return transformCondition(eventType, patientUuid);
            }
        }*/


        switch (ObservationType.fromValue(eventType.getEventType().intValue())) {

            case TEXT:
            case OBSERVATION:
                return ObservationTransformer.transform(eventType, patientUuid);
            case PROBLEM:
            case VALUE:
                return ObservationTransformer.transform(eventType, patientUuid);
            case ATTACHMENT:
            case REFERRAL:
            case ALERT:
                return null;
            case ALLERGY:
                return AllergyTransformer.transform(eventType, patientUuid);
            case FAMILYHISTORY:
                return FamilyHistoryTransformer.transform(eventType, patientUuid);
            case IMMUNISATION:
                return ImmunisationTransformer.transform(eventType, patientUuid);
            case PROBLEMRATING:
                return null;
            default:
                throw new TransformException("Unhandled event type " + eventType);
        }
    }

    private static Resource transformCondition(EventType eventType, String patientUuid) throws TransformException {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirCondition, patientUuid, eventGuid);

        fhirCondition.setPatient(EmisOpenHelper.createPatientReference(patientUuid));

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            fhirCondition.setAsserter(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        if (dateRecorded != null) {
            fhirCondition.setDateRecorded(dateRecorded);
        }

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirCondition, recordedByGuid);

        fhirCondition.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed
        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        fhirCondition.setOnset(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirCondition.setNotes(text);
        }

        return fhirCondition;
    }

    public static Resource transformProcedure(EventType eventType, String patientGuid) throws TransformException {
        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirProcedure, patientGuid, eventGuid);

        fhirProcedure.setSubject(EmisOpenHelper.createPatientReference(patientGuid));
        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        fhirProcedure.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        fhirProcedure.setPerformed(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirProcedure.addNotes(AnnotationHelper.createAnnotation(text));
        }

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            Procedure.ProcedurePerformerComponent fhirPerformer = fhirProcedure.addPerformer();
            fhirPerformer.setActor(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirProcedure, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirProcedure, recordedByGuid);

        return fhirProcedure;
    }

    private static Date findRecordedDate(AuthorType authorType) throws TransformException {
        if (authorType == null) {
            return null;
        }
        String dateStr = authorType.getSystemDate();
        if (Strings.isNullOrEmpty(dateStr)) {
            return null;
        }

        return DateConverter.getDate(dateStr);
    }

    private static String findRecordedUserGuid(AuthorType authorType) {
        if (authorType == null) {
            return null;
        }
        IdentType identType = authorType.getUser();
        if (identType == null) {
            return null;
        }

        return identType.getGUID();
    }

    private static void addRecordedByExtension(DomainResource resource, String recordedByGuid) throws TransformException {
        if (Strings.isNullOrEmpty(recordedByGuid)) {
            return;
        }

        Reference reference = EmisOpenHelper.createPractitionerReference(recordedByGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
    }

    private static void addRecordedDateExtension(DomainResource resource, Date recordedDate) throws TransformException {
        if (recordedDate == null) {
            return;
        }

        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(recordedDate)));
    }
}
