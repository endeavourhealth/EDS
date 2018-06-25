package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.framework.exceptions.BadRequestException;
import org.endeavourhealth.ui.json.JsonResourceContainer;
import org.endeavourhealth.ui.json.JsonResourceType;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/resources")
@Metrics(registry = "EdsRegistry")
public class ResourceEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceEndpoint.class);

    private static final ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
    //private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Resource);
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resourceTypesForPatient")
    public Response getResourceTypesUsed(@Context SecurityContext sc,
                          @QueryParam("patientId") String patientIdStr) throws Exception {

        super.setLogbackMarkers(sc);

        List<JsonResourceType> ret = new ArrayList<>();

        //if an empty or partial patient ID is passed up, just return an empty list, rather than failing with a bad request error,
        //as it's easier than validating the patient ID on the client
        if (!Strings.isNullOrEmpty(patientIdStr)) {
            try {
                UUID patientId = UUID.fromString(patientIdStr);

                ResourceHistory patientResource = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), patientId);
                if (patientResource != null) {

                    UUID serviceId = patientResource.getServiceId();
                    UUID systemId = patientResource.getSystemId();

                    List<ResourceTypesUsed> resourcesTypesUsed = resourceRepository.getResourcesTypesUsed(serviceId, systemId);
                    for (ResourceTypesUsed r : resourcesTypesUsed) {
                        ret.add(new JsonResourceType(r.getResourceType()));
                    }
                }
            } catch (IllegalArgumentException ex) {
                //do nothing if the string isn't a valid UUID
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }*/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ResourceEndpoint.GetAllResourceTypes")
    @Path("/allResourceTypes")
    public Response getAllResourceTypes(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

        List<JsonResourceType> ret = new ArrayList<>();

        for (ResourceType r : ResourceType.class.getEnumConstants()) {
            ret.add(new JsonResourceType(r.toString()));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ResourceEndpoint.GetForId")
    @Path("/forId")
    public Response forId(@Context SecurityContext sc,
                          @QueryParam("serviceId") String serviceId,
                          @QueryParam("resourceType") String resourceType,
                          @QueryParam("resourceId") String resourceId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Resource",
            "Version", "Current",
            "Resource Type", resourceType,
            "Resource Id", resourceId);

        if (Strings.isNullOrEmpty(resourceType)) {
            throw new BadRequestException("Resource Type must be selected");
        }
        if (Strings.isNullOrEmpty(resourceId)) {
            throw new BadRequestException("Resource ID must be entered");
        }

        List<JsonResourceContainer> ret = new ArrayList<>();

        ResourceWrapper resourceHistory = resourceRepository.getCurrentVersion(UUID.fromString(serviceId), resourceType, UUID.fromString(resourceId));
        if (resourceHistory != null) {
            ret.add(new JsonResourceContainer(resourceHistory));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ResourceEndpoint.GetForPatient")
    @Path("/forPatient")
    public Response forPatient(@Context SecurityContext sc,
                               @QueryParam("serviceId") String serviceId,
                               @QueryParam("resourceType") String resourceType,
                               @QueryParam("patientId") String patientIdStr) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Resource For Patient",
            "Resource Type", resourceType,
            "Patient Id", patientIdStr);

        super.setLogbackMarkers(sc);

        if (Strings.isNullOrEmpty(resourceType)) {
            throw new BadRequestException("Resource Type must be selected");
        }
        if (Strings.isNullOrEmpty(patientIdStr)) {
            throw new BadRequestException("Patient ID must be entered");
        }

        UUID patientId = UUID.fromString(patientIdStr);

        List<JsonResourceContainer> ret = new ArrayList<>();

        ResourceWrapper patientResource = resourceRepository.getCurrentVersion(UUID.fromString(serviceId), ResourceType.Patient.toString(), patientId);
        if (patientResource != null) {

            UUID systemId = patientResource.getSystemId();

            List<ResourceWrapper> resourceHistories = resourceRepository.getResourcesByPatient(UUID.fromString(serviceId), patientId, resourceType);
            for (ResourceWrapper resourceHistory: resourceHistories) {
                ret.add(new JsonResourceContainer(resourceHistory));
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ResourceEndpoint.GetResourceHistory")
    @Path("/resourceHistory")
    public Response resourceHistory(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceId,
                                    @QueryParam("resourceType") String resourceType,
                                    @QueryParam("resourceId") String resourceId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Resource History",
            "Version", "History",
            "Resource Type", resourceType,
            "Resource Id", resourceId);

        if (Strings.isNullOrEmpty(resourceType)) {
            throw new BadRequestException("Resource Type must be selected");
        }
        if (Strings.isNullOrEmpty(resourceId)) {
            throw new BadRequestException("Resource ID must be entered");
        }

        List<JsonResourceContainer> ret = new ArrayList<>();

        List<ResourceWrapper> resourceHistories = resourceRepository.getResourceHistory(UUID.fromString(serviceId), resourceType, UUID.fromString(resourceId));
        for (ResourceWrapper resourceHistory: resourceHistories) {
            ret.add(new JsonResourceContainer(resourceHistory));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

}
