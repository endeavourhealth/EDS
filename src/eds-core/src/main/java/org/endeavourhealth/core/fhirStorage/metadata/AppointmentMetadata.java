package org.endeavourhealth.core.fhirStorage.metadata;

import org.endeavourhealth.common.utility.StreamExtension;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class AppointmentMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public AppointmentMetadata(Appointment resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Appointment resource) {
        patientId = resource.getParticipant()
                .stream()
                .filter(p -> p.hasActor() && ReferenceHelper.getReferenceType(p.getActor()).equals(ResourceType.Patient))
                .map(p -> UUID.fromString(ReferenceHelper.getReferenceId(p.getActor(), ResourceType.Patient)))
                .collect(StreamExtension.firstOrNullCollector());
    }
}
