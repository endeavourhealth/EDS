package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperAppointment extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Appointment appointment = (Appointment)resource;

        super.mapResourceId(appointment, serviceId, systemInstanceId);
        super.mapExtensions(appointment, serviceId, systemInstanceId);

        if (appointment.hasIdentifier()) {
            super.mapIdentifiers(appointment.getIdentifier(), serviceId, systemInstanceId);
        }
        if (appointment.hasSlot()) {
            super.mapReferences(appointment.getSlot(), serviceId, systemInstanceId);
        }
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participant: appointment.getParticipant()) {
                if (participant.hasActor()) {
                    super.mapReference(participant.getActor(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
