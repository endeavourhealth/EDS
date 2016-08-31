package org.endeavourhealth.core.fhirStorage;

import org.endeavourhealth.core.fhirStorage.statistics.PatientStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.ResourceStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.StorageStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageStatisticsService {
    public StorageStatistics getStatistics(UUID serviceId, UUID systemId, List<String> resourceNames) {
        StorageStatistics response = new StorageStatistics();

        response.setServiceId(serviceId);
        response.setSystemId(systemId);

        PatientStatistics patientStatistics = new PatientStatistics();
        patientStatistics.setTotalCount(20000);
        patientStatistics.setActiveCount(10000);
        patientStatistics.setDeceasedCount(5000);
        response.setPatientStatistics(patientStatistics);

        List<ResourceStatistics> resourceStatistics = new ArrayList<>();
        for (String resourceName :resourceNames) {
            ResourceStatistics resourceStat = new ResourceStatistics();
            resourceStat.setResourceName(resourceName);
            resourceStat.setTotalCount(100000);
            resourceStat.setDeletedCount(0);
            resourceStatistics.add(resourceStat);
        }
        response.setResourceStatistics(resourceStatistics);

        return response;
    }
}
