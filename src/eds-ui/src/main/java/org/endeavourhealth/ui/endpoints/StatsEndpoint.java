package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.fhirStorage.statistics.PatientStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.ResourceStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.StorageStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.StorageStatisticsService;
import org.endeavourhealth.core.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/stats")
public final class StatsEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(StatsEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getStorageStatistics")
    public Response getStorageStatistics(@Context SecurityContext sc, @QueryParam("serviceList") List<String> services) throws Exception {

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        UUID systemId = UUID.fromString("0eda7854-ebdf-4a11-97a1-79c681968863");

        LOG.trace("getStorageStatistics {}", services);

        PatientStatistics patientStats = new PatientStatistics();
        List<ResourceStatistics> resourceStats = new ArrayList<>();

        List<StorageStatistics> statsList = new ArrayList<>();

        StorageStatisticsService statisticsService = new StorageStatisticsService();

        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("Observation");
        resourceNames.add("MedicationOrder");
        resourceNames.add("Condition");
        resourceNames.add("AllergyIntolerance");
        resourceNames.add("Procedure");
        resourceNames.add("ReferralRequest");
        resourceNames.add("Appointment");
        resourceNames.add("Encounter");

        for (String serviceId :services) {
            patientStats = statisticsService.getPatientStatistics(UUID.fromString(serviceId), systemId);
            resourceStats = statisticsService.getResourceStatistics(UUID.fromString(serviceId), systemId, resourceNames);
            StorageStatistics storageStatistics = new StorageStatistics();
            storageStatistics.setServiceId(UUID.fromString(serviceId));
            storageStatistics.setSystemId(systemId);
            storageStatistics.setPatientStatistics(patientStats);
            storageStatistics.setResourceStatistics(resourceStats);
            statsList.add(storageStatistics);
        }

        return Response
                .ok()
                .entity(statsList)
                .build();
    }


}
