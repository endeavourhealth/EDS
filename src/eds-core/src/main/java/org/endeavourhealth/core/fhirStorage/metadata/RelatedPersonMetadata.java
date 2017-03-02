package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.RelatedPerson;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class RelatedPersonMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public RelatedPersonMetadata(RelatedPerson resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(RelatedPerson resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
