package org.endeavourhealth.core.fhirStorage.statistics;

import org.endeavourhealth.core.data.ehr.ResourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageStatisticsService {
    private static final String PATIENT_RESOURCE_TYPE_NAME = "Patient";

    private final ResourceRepository repository;

    public StorageStatisticsService() {
        this.repository = new ResourceRepository();
    }

    public PatientStatistics getPatientStatistics(UUID serviceId, UUID systemId) {
        return createPatientStatistics(serviceId, systemId);
    }

    public List<ResourceStatistics> getResourceStatistics(UUID serviceId, UUID systemId, List<String> resourceTypes) {
        List<ResourceStatistics> results = new ArrayList<>();

        for (String resourceType :resourceTypes) {
            results.add(createResourceStatistics(serviceId, systemId, resourceType));
        }

        return results;
    }

    private PatientStatistics createPatientStatistics(UUID serviceId, UUID systemId) {
        PatientStatistics statistics = new PatientStatistics();
        statistics.setTotalCount(repository.getResourceCountByService(serviceId, systemId, PATIENT_RESOURCE_TYPE_NAME));
        return statistics;
    }

    private ResourceStatistics createResourceStatistics(UUID serviceId, UUID systemId, String resourceType) {
        ResourceStatistics statistics = new ResourceStatistics(resourceType);
        statistics.setTotalCount(repository.getResourceCountByService(serviceId, systemId, resourceType));
        return statistics;
    }
}
