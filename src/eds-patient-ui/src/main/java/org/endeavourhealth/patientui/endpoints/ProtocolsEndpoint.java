package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/audit")
public class ProtocolsEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ProtocolsEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getProtocols")
    public Response login(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);


        clearLogbackMarkers();



        return Response
                .ok()
                //.entity(ret)
                .build();
    }
}
