package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.ui.json.JsonPatientIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/trace")
public final class PatientIdentityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(PatientIdentityEndpoint.class);

    private final PatientIdentifierRepository repository = new PatientIdentifierRepository();
    private final ServiceRepository serviceRepository = new ServiceRepository();
    private final LibraryRepository libraryRepository = new LibraryRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ByNhsNumber")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, @QueryParam("nhsNumber") String nhsNumber) throws Exception {
        super.setLogbackMarkers(sc);

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        List<PatientIdentifierByNhsNumber> identifiers = repository.getForNhsNumber(nhsNumber);
        for (PatientIdentifierByNhsNumber identifier: identifiers) {

            UUID serviceId = identifier.getServiceId();
            UUID systemId = identifier.getSystemId();

            Service service = serviceRepository.getById(serviceId);
            String serviceName = service.getName();

            ActiveItem activeItem = libraryRepository.getActiveItemByItemId(serviceId);
            Item item = libraryRepository.getItemByKey(serviceId, activeItem.getAuditId());
            String systemName = item.getTitle();

/*            JsonPatientIdentifier json = new JsonPatientIdentifier();
            json.setServiceId(serviceId);
            json.setServiceName(serviceName);
            json.setSystemId(systemId);
            json.setSystemName();
            json.setNhsNumber(identifier.getNhsNumber());
            json.setForenames(identifier.getForenames());
            json.setSurname(identifier.getSurname());
            json.setDateOfBirth(identifier.getDateOfBirth());
            json.setPostcode(identifier.getPostcode());
            json.setGender(identifier.getGender());
            json.setPatientId(identifier.getPatientId());
            json.setLocalId(identifier.getLocalId());
            json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);*/

        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}

