package org.endeavourhealth.ui.endpoints;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.business.recordViewer.RecordViewerBusiness;
import org.endeavourhealth.ui.json.JsonPatient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

@Path("/recordViewer")
public final class RecordViewerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerEndpoint.class);

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

        JsonPatient patient = RecordViewerBusiness.getDemographics(serviceId, systemId, patientId);

        return Response
                .ok()
                .entity(patient)
                .build();
    }
}