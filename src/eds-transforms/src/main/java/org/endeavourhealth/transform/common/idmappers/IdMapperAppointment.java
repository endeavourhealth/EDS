package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperAppointment extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Appointment appointment = (Appointment)resource;

        if (appointment.hasIdentifier()) {
            super.mapIdentifiers(appointment.getIdentifier(), resource, serviceId, systemId);
        }
        if (appointment.hasSlot()) {
            super.mapReferences(appointment.getSlot(), resource, serviceId, systemId);
        }
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participant: appointment.getParticipant()) {
                if (participant.hasActor()) {
                    super.mapReference(participant.getActor(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(appointment, serviceId, systemId, mapResourceId);
    }
}
