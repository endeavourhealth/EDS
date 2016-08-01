package org.endeavourhealth.ui.endpoints_public;

import org.endeavourhealth.ui.endpoints_public.models.AuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/wellknown")
public final class WellKnownEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(WellKnownEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/authconfig")
    public Response authconfig() {

        // TODO: read this config from the database
        AuthConfig config = new AuthConfig(
                "Endeavour",
                "http://localhost:9080/auth",
                "example-app",
                "http://localhost:8080"
        );

        // IMPORTANT: Do NOT put anything sensitive in this config return,
        //            it is intended to be used to configure the front-end
        //            app by passing configuration stored on disk or in
        //            a database

        return Response
                .ok()
                .entity(config)
                .build();
    }
}