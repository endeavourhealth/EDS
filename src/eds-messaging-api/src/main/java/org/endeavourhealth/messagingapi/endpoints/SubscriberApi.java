package org.endeavourhealth.messagingapi.endpoints;

import com.google.common.base.Strings;
import com.sun.javaws.exceptions.InvalidArgumentException;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/subscriber")
public class SubscriberApi {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApi.class);

    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"eds_read", "eds_read_only", "eds_read_write"})
    public Response uploadFiles(@Context HttpServletRequest request,
                                @Context UriInfo uriInfo,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode) throws Exception{

        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        String subject = null;
        String code = null;

        for (String key: params.keySet()) {
            String value = params.getFirst(key);

            if (key.equalsIgnoreCase("subject")) {
                subject = value;

            } else if (key.equalsIgnoreCase("code")) {
                code = value;

            } else {
                throw new InvalidArgumentException(new String[] { key });
            }
        }

        //validate all expected parameters and headers are therer
        if (Strings.isNullOrEmpty(resourceTypeRequested)) {
            throw new Exception("Missing resource type requested from URL path");
        }

        if (Strings.isNullOrEmpty(headerOdsCode)) {
            throw new Exception("Missing OdsCode from request headers");
        }

        if (Strings.isNullOrEmpty(subject)) {
            throw new Exception("Missing subject parameter");
        }

        if (Strings.isNullOrEmpty(code)) {
            throw new Exception("Missing code parameter");
        }

        //validate the parameters match what we're expecting
        if (!resourceTypeRequested.equalsIgnoreCase("flag")) {
            throw new Exception("Only flag FHIR reource types can be requested");
        }

        if (!code.equalsIgnoreCase("289999999105")) {
            throw new Exception("Only code 289999999105 can be requested");
        }


        //code =
        //subject
        //resource type
        //ods code


        LOG.info("Called my API");
        LOG.info("Called my API");

        //validate ODS code against service ID from keycloak???
        //find patient in Discovery
        //if no patient, return an error
        //get patient owning organisation(s)
        //validate protocol exists between requester and owning organisation
        //return FHIR resource
        //change exceptions to property ones with proper error codes

        return null;
    }

    /*@GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"eds_read", "eds_read_only", "eds_read_write"})
    public Response uploadFiles(@Context HttpServletRequest request,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode) throws Exception{

        //TODO - does this function need the @RolesAllowed({"xxxx"}) attribute?

        LOG.info("Called my API");
        LOG.info("Called my API");

        //get code and subject (patient) from request parameters
        //validate ODS code against service ID from keycloak???
        //find patient in Discovery
        //if no patient, return an error
        //get patient owning organisation(s)
        //validate protocol exists between requester and owning organisation
        //return FHIR resource

        return null;
    }*/



}
