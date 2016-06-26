package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.core.data.ehr.PersonResourceRepository;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.patientui.framework.security.TokenHelper;
import org.endeavourhealth.patientui.framework.security.Unsecured;
import org.endeavourhealth.patientui.json.JsonLoginParameters;
import org.endeavourhealth.patientui.json.JsonUser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/patientRecord")
public final class PatientRecordEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(PatientRecordEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/forServiceId")
    public Response login(@Context SecurityContext sc, @QueryParam("serviceId") String serviceId) throws Exception {
        super.setLogbackMarkers(sc);

        //TODO - change EHR model to allow getting all resources
        UUID personId = getPersonIdFromSession(sc);

        PersonResourceRepository repository = new PersonResourceRepository();
        Iterable<PersonResource> personResources = repository.getByService(personId,
                "Organization", UUID.fromString(serviceId));

        //List<Resource> ret = new ArrayList<>();
        List<String> ret = new ArrayList<>();

        for (PersonResource personResource: personResources) {

            String json = personResource.getResourceData();
            ret.add(json);
            //JsonParser p = new JsonParser();
            //Resource resource = p.parse(json);
            //ret.add(resource);
        }

        clearLogbackMarkers();

        String json = ret.get(0);
        return Response
                .ok()
                .entity(json).type(MediaType.APPLICATION_JSON_TYPE)
                .build();

/*        return Response
                .ok()
                .entity(ret)
                .build();*/
    }



}
