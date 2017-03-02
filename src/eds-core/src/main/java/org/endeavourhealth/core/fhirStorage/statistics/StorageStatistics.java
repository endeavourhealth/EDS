package org.endeavourhealth.core.fhirStorage.statistics;

import java.util.List;
import java.util.UUID;

public class StorageStatistics {
    private UUID serviceId;
    private UUID systemId;

    private PatientStatistics patientStatistics;

    private List<ResourceStatistics> resourceStatistics;

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public PatientStatistics getPatientStatistics() {
        return patientStatistics;
    }

    public void setPatientStatistics(PatientStatistics patientStatistics) {
        this.patientStatistics = patientStatistics;
    }

    public List<ResourceStatistics> getResourceStatistics() {
        return resourceStatistics;
    }

    public void setResourceStatistics(List<ResourceStatistics> resourceStatistics) {
        this.resourceStatistics = resourceStatistics;
    }
}
