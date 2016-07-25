package org.endeavourhealth.patientui.endpoints;

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

@Path("/audit")
public class ProtocolsEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalRecordEndpoint.class);

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
