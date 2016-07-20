package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class OrderMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public OrderMetadata(Order resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Order resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
