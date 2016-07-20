package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class DiagnosticReportMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public DiagnosticReportMetadata(DiagnosticReport resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(DiagnosticReport resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
