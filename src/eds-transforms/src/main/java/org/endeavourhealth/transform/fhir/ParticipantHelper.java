package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.common.TransformException;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.ResourceType;

public class ParticipantHelper {

    public static Appointment.AppointmentParticipantComponent createParticipant(ResourceType resourceType, String id) throws TransformException {
        return createParticipant(resourceType, id, Appointment.ParticipantRequired.REQUIRED, Appointment.ParticipationStatus.ACCEPTED);
    }
    public static Appointment.AppointmentParticipantComponent createParticipant(ResourceType resourceType, String id, Appointment.ParticipantRequired required, Appointment.ParticipationStatus status) throws TransformException {
        return new Appointment.AppointmentParticipantComponent()
                .setActor(ReferenceHelper.createReference(resourceType, id))
                .setRequired(required)
                .setStatus(status);
    }
}
