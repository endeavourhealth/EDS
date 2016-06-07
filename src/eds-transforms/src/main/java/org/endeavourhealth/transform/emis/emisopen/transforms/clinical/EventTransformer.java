package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.EventType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class EventTransformer
{
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

    public static List<Resource> transform(MedicalRecordType medicalRecordType) throws TransformException
    {
        List<Resource> resource = new ArrayList<>();

        for (EventType eventType : medicalRecordType.getEventList().getEvent())
            resource.add(transform(eventType, medicalRecordType.getRegistration().getGUID()));

        return resource;
    }

    private static Resource transform(EventType eventType, String patientUuid) throws TransformException
    {
        switch (ObservationType.fromValue(eventType.getEventType().intValue()))
        {
            case TEXT:
            case OBSERVATION:
            case PROBLEM:
            case VALUE:
            case ATTACHMENT:
            case REFERRAL:
            case ALERT: return null;
            case ALLERGY: return transformAllergy(eventType, patientUuid);
            case FAMILYHISTORY:
            case IMMUNISATION:
            case PROBLEMRATING: return null;
        }

        return null;
    }

    private static AllergyIntolerance transformAllergy(EventType eventType, String patientUuid) throws TransformException
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
