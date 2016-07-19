package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.patientui.json.JsonLoginParameters;
import org.endeavourhealth.patientui.json.JsonUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.UUID;

@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@Context SecurityContext sc, JsonLoginParameters loginParameters) throws Exception {
        super.setLogbackMarkers(sc);

        String username = loginParameters.getUsername();
        String password = loginParameters.getPassword();

        LOG.trace("Login for {}", username);

        if (username == null
                || username.length() == 0
                || password == null
                || password.length() == 0) {
            throw new BadRequestException("Missing username or password in request");
        }

        //THIS IS A STUB FUNCTION TO BE REPLACED WITH A CALLOUT TO HEALTHFORGE TO AUTHENTICATE PATIENTS

        //Assume we can get these identifying details about our patient from the Authentication service
        String nhsNumber = "1111111111";
        String title = "Mr";
        String forename = "Forename";
        String middleNames = "Middlename";
        String surname = "Surname";
        Date dob = new Date();
        UUID personId = UUID.fromString("19a4b78c-eb28-4b82-8f55-741a35179bb0");

        JsonUser ret = new JsonUser();
        ret.setNhsNumber(nhsNumber);
        ret.setTitle(title);
        ret.setForename(forename);
        ret.setMiddleNames(middleNames);
        ret.setSurname(surname);
        ret.setDob(dob);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/logoff")
    public Response logoff(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("Logoff");

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }



}