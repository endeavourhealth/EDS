package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.fhirStorage.statistics.PatientStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.ResourceStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.StorageStatistics;
import org.endeavourhealth.core.fhirStorage.statistics.StorageStatisticsService;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
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
@Metrics(registry = "EdsRegistry")
public final class StatsEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(StatsEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Stats);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.StatsEndpoint.GetStorageStatistics")
    @Path("/getStorageStatistics")
    public Response getStorageStatistics(@Context SecurityContext sc,
                                         @QueryParam("serviceList") List<String> services,
                                         @QueryParam("systemList") List<String> systems) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Statistics",
            "Service list", services,
            "System list", systems);

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

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
        resourceNames.add("Immunization");
        resourceNames.add("ReferralRequest");
        resourceNames.add("Appointment");
        resourceNames.add("Encounter");

        Integer i = 0;

        for (String serviceId :services) {
            UUID systemId = UUID.fromString(systems.get(i)); //TODO: pick first system registered against service for now - later offer choice

            patientStats = statisticsService.getPatientStatistics(UUID.fromString(serviceId), systemId);
            resourceStats = statisticsService.getResourceStatistics(UUID.fromString(serviceId), systemId, resourceNames);
            StorageStatistics storageStatistics = new StorageStatistics();
            storageStatistics.setServiceId(UUID.fromString(serviceId));
            storageStatistics.setSystemId(systemId);
            storageStatistics.setPatientStatistics(patientStats);
            storageStatistics.setResourceStatistics(resourceStats);
            statsList.add(storageStatistics);

            i++;
        }

        return Response
                .ok()
                .entity(statsList)
                .build();
    }


}
