package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientui.json.JsonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/medicalRecord")
public class MedicalRecordEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MedicalRecordEndpoint.class);

    private static PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();
    private static ServiceRepository serviceRepository = new ServiceRepository();
    private static OrganisationRepository organisationRepository = new OrganisationRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getServices")
    public Response getServices(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        String nhsNumber = getNhsNumberFromSession(sc);
        List<PatientIdentifierByNhsNumber> identifiers = identifierRepository.getForNhsNumber(nhsNumber);

        // Maintain map to prevent duplicates where multiple local id's exist
        Map<UUID, JsonService> services = new HashMap<>();

        for (PatientIdentifierByNhsNumber identifier: identifiers) {
            UUID serviceId = identifier.getServiceId();

            if (!services.containsKey(serviceId)) {
                Service service = serviceRepository.getById(serviceId);
                // TODO : Many-to-many org-service mapping.  Currently assumes 1:1
                UUID organisationUuid = (UUID)service.getOrganisations().keySet().toArray()[0];
                Organisation org = organisationRepository.getById(organisationUuid);

                JsonService jsonService = new JsonService();
                jsonService.setOrganisationId(organisationUuid.toString());
                jsonService.setOrganisationName(org.getName());
                jsonService.setOrganisationNationalId(org.getNationalId());
                jsonService.setServiceId(serviceId.toString());
                jsonService.setServiceName(service.getName());
                services.put(serviceId, jsonService);
            }
        }

        return Response
                .ok()
                .entity(services.values())
                .build();
    }
}
