package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.ProcedureRequest;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ProcedureRequestMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ProcedureRequestMetadata(ProcedureRequest resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(ProcedureRequest resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
