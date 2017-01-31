package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

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

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Appointment appointment = (Appointment)resource;
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participant: appointment.getParticipant()) {
                String id = ReferenceHelper.getReferenceId(participant.getActor(), ResourceType.Patient);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }
}
