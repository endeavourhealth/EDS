package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByPatientId;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.ehr.models.ResourceTypesUsed;
import org.endeavourhealth.ui.json.JsonResourceContainer;
import org.endeavourhealth.ui.json.JsonResourceType;
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
public class ResourceEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceEndpoint.class);

    private static final ResourceRepository resourceRepository = new ResourceRepository();
    private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/forId")
    public Response forId(@Context SecurityContext sc,
                          @QueryParam("resourceType") String resourceType,
                          @QueryParam("resourceId") String resourceId) throws Exception {

        super.setLogbackMarkers(sc);

        JsonResourceContainer ret = null;

        ResourceHistory resourceHistory = resourceRepository.getCurrentVersion(resourceType, UUID.fromString(resourceId));
        if (resourceHistory != null) {
            ret = new JsonResourceContainer(resourceHistory);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resourceHistory")
    public Response resourceHistory(@Context SecurityContext sc,
                                      @QueryParam("resourceType") String resourceType,
                                      @QueryParam("resourceId") String resourceId) throws Exception {

        super.setLogbackMarkers(sc);

        List<JsonResourceContainer> ret = new ArrayList<>();

        List<ResourceHistory> resourceHistories = resourceRepository.getResourceHistory(resourceType, UUID.fromString(resourceId));
        for (ResourceHistory resourceHistory: resourceHistories) {
            ret.add(new JsonResourceContainer(resourceHistory));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/forPatient")
    public Response forPatient(@Context SecurityContext sc,
                              @QueryParam("patientId") String patientIdStr,
                              @QueryParam("resourceType") String resourceType) throws Exception {

        super.setLogbackMarkers(sc);

        UUID patientId = UUID.fromString(patientIdStr);

        List<JsonResourceContainer> ret = new ArrayList<>();

        PatientIdentifierByPatientId identifier = identifierRepository.getMostRecentByPatientId(patientId);
        if (identifier != null) {

            UUID serviceId = identifier.getServiceId();
            UUID systemId = identifier.getSystemId();

            List<ResourceByPatient> resourceHistories = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId, resourceType);
            for (ResourceByPatient resourceHistory: resourceHistories) {
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
    @Path("/resourceTypesUsed")
    public Response resourceTypesUsed(@Context SecurityContext sc,
                                      @QueryParam("serviceId") String serviceIdStr,
                                      @QueryParam("systemId") String systemIdStr) throws Exception {

        super.setLogbackMarkers(sc);

        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);

        List<JsonResourceType> ret = new ArrayList<>();

        List<ResourceTypesUsed> resourceTypes = new ResourceRepository().getResourcesTypesUsed(serviceId, systemId);
        for (ResourceTypesUsed resourceTypesUsed: resourceTypes) {
            ret.add(new JsonResourceType(resourceTypesUsed.getResourceType()));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
