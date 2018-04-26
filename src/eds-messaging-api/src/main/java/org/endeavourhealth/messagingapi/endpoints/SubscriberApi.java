package org.endeavourhealth.messagingapi.endpoints;

import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/subscriber")
public class SubscriberApi {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApi.class);

    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFiles(@Context HttpServletRequest request,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String orgRoleId,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode) throws Exception{

        //TODO - does this function need the @RolesAllowed({"xxxx"}) attribute?

        LOG.info("Called my API");
        LOG.info("Called my API");

        return null;
    }
}
