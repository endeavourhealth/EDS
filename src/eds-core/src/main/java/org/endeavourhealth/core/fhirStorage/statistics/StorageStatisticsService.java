package org.endeavourhealth.core.fhirStorage.statistics;

import org.endeavourhealth.core.data.ehr.ResourceMetadataIterator;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.fhirStorage.metadata.PatientMetadata;

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
        long totalCount = 0;
        long activeCount = 0;
        long deceasedCount = 0;

        ResourceMetadataIterator<PatientMetadata> patientMetadataIterator = repository.getMetadataByService(serviceId,
                systemId,
                PATIENT_RESOURCE_TYPE_NAME,
                PatientMetadata.class);

        while(patientMetadataIterator.hasNext()) {
            PatientMetadata patientMetadata = patientMetadataIterator.next();

            if (patientMetadata != null) {
                if (patientMetadata.isDeceased()) {
                    deceasedCount++;
                }
                else if (patientMetadata.isActive()) {
                    activeCount++;
                }
                totalCount++;
            }

        }

        PatientStatistics statistics = new PatientStatistics();
        statistics.setTotalCount(totalCount);
        statistics.setActiveCount(activeCount);
        statistics.setDeceasedCount(deceasedCount);
        return statistics;
    }

    private ResourceStatistics createResourceStatistics(UUID serviceId, UUID systemId, String resourceType) {
        ResourceStatistics statistics = new ResourceStatistics(resourceType);
        statistics.setTotalCount(repository.getResourceCountByService(serviceId, systemId, resourceType));
        return statistics;
    }
}
