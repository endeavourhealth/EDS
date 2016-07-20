package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.ReferralRequest;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ReferralRequestMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ReferralRequestMetadata(ReferralRequest resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(ReferralRequest resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
