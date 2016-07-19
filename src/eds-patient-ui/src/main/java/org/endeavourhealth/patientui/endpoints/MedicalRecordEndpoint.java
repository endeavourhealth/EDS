package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.PersonIdentifierByNhsNumberRepository;
import org.endeavourhealth.core.data.ehr.PersonResourceRepository;
import org.endeavourhealth.core.data.ehr.models.PersonIdentifierByNhsNumber;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.patientui.json.JsonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/medicalRecord")
public class MedicalRecordEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalRecordEndpoint.class);

    private static PersonIdentifierByNhsNumberRepository identifierRepository = new PersonIdentifierByNhsNumberRepository();
    //private static ServiceRepository serviceRepository = new ServiceRepository();
    private static OrganisationRepository organisationRepository = new OrganisationRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getServices")
    public Response getServices(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        String nhsNumber = getNhsNumberFromSession(sc);
        List<PersonIdentifierByNhsNumber> identifiers = identifierRepository.getForNhsNumber(nhsNumber);

        List<JsonService> ret = new ArrayList<>();

        for (PersonIdentifierByNhsNumber identifier: identifiers) {
            UUID serviceId = identifier.getSystemInstanceId();
            UUID orgId = identifier.getServiceId();

            Organisation org = organisationRepository.getById(orgId);
            Map<UUID, String> serviceDetails = org.getServices();
            String serviceName = serviceDetails.get(serviceId);

            JsonService jsonService = new JsonService();
            jsonService.setOrganisationId(orgId.toString());
            jsonService.setOrganisationName(org.getName());
            jsonService.setOrganisationNationalId(org.getNationalId());
            jsonService.setServiceId(serviceId.toString());
            jsonService.setServiceName(serviceName);
            ret.add(jsonService);
        }

        return Response
                .ok()
                .entity(ret)
                .build();
    }

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
