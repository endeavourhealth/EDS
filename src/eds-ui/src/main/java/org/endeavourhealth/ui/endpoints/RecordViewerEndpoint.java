package org.endeavourhealth.ui.endpoints;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.ui.json.JsonDemographics;
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
import java.util.List;
import java.util.UUID;

@Path("/recordViewer")
public final class RecordViewerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerEndpoint.class);
    private static final String RESOURCE_TYPE_PATIENT = "Patient";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getDemographics")
    public Response getDemographics(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceIdString,
                                    @QueryParam("systemId") String systemIdString,
                                    @QueryParam("patientId") String patientIdString) throws Exception {

        Validate.notBlank(serviceIdString, "serviceId");
        Validate.notBlank(systemIdString, "systemId");
        Validate.notBlank(patientIdString, "patientId");

        UUID serviceId = UUID.fromString(serviceIdString);
        UUID systemId = UUID.fromString(systemIdString);
        UUID patientId = UUID.fromString(patientIdString);

        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByPatient> resourceByPatientList = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId, RESOURCE_TYPE_PATIENT);

        JsonDemographics demographics = new JsonDemographics();
        demographics.setServiceId(serviceId);
        demographics.setSystemId(systemId);
        demographics.setPatientId(patientId);
        demographics.setNhsNumber("9999999999");

        return Response
                .ok()
                .entity(demographics)
                .build();
    }
}