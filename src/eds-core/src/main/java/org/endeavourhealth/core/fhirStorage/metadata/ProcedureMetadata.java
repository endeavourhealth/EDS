package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Procedure;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ProcedureMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ProcedureMetadata(Procedure resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Procedure resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
