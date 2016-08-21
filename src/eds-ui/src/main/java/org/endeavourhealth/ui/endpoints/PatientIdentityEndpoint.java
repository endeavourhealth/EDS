package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByPatientId;
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

@Path("/tracePatient")
public final class PatientIdentityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(PatientIdentityEndpoint.class);

    private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final LibraryRepository libraryRepository = new LibraryRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ByLocalIdentifier")
    public Response byLocalIdentifier(@Context SecurityContext sc,
                         @QueryParam("serviceId") String serviceIdStr,
                         @QueryParam("systemId") String systemIdStr,
                         @QueryParam("localId") String localId,
                         @QueryParam("localIdSystem") String localIdSystem) throws Exception {

        super.setLogbackMarkers(sc);

        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        PatientIdentifierByLocalId identifier = identifierRepository.getMostRecentByLocalId(serviceId, systemId, localId, localIdSystem);
        if (identifier != null) {

            String serviceName = getServiceNameForId(serviceId);
            String systemName = getSystemNameForId(systemId);

            JsonPatientIdentifier json = new JsonPatientIdentifier();
            json.setServiceId(serviceId);
            json.setServiceName(serviceName);
            json.setSystemId(systemId);
            json.setSystemName(systemName);
            json.setNhsNumber(identifier.getNhsNumber());
            json.setForenames(identifier.getForenames());
            json.setSurname(identifier.getSurname());
            json.setDateOfBirth(identifier.getDateOfBirth());
            json.setPostcode(identifier.getPostcode());
            json.setGender(identifier.getGender().getDisplay());
            json.setPatientId(identifier.getPatientId());
            json.setLocalId(identifier.getLocalId());
            json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ByNhsNumber")
    public Response byNhsNumber(@Context SecurityContext sc, @QueryParam("nhsNumber") String nhsNumber) throws Exception {
        super.setLogbackMarkers(sc);

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        List<PatientIdentifierByNhsNumber> identifiers = identifierRepository.getForNhsNumber(nhsNumber);
        for (PatientIdentifierByNhsNumber identifier: identifiers) {

            UUID serviceId = identifier.getServiceId();
            UUID systemId = identifier.getSystemId();

            String serviceName = getServiceNameForId(serviceId);
            String systemName = getSystemNameForId(systemId);

            JsonPatientIdentifier json = new JsonPatientIdentifier();
            json.setServiceId(serviceId);
            json.setServiceName(serviceName);
            json.setSystemId(systemId);
            json.setSystemName(systemName);
            json.setNhsNumber(identifier.getNhsNumber());
            json.setForenames(identifier.getForenames());
            json.setSurname(identifier.getSurname());
            json.setDateOfBirth(identifier.getDateOfBirth());
            json.setPostcode(identifier.getPostcode());
            json.setGender(identifier.getGender().getDisplay());
            json.setPatientId(identifier.getPatientId());
            json.setLocalId(identifier.getLocalId());
            json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ByPatientId")
    public Response byPatientId(@Context SecurityContext sc, @QueryParam("patientId") String patientIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        UUID patientId = UUID.fromString(patientIdStr);

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        PatientIdentifierByPatientId identifier = identifierRepository.getMostRecentByPatientId(patientId);
        if (identifier != null) {

            UUID serviceId = identifier.getServiceId();
            UUID systemId = identifier.getSystemId();

            String serviceName = getServiceNameForId(serviceId);
            String systemName = getSystemNameForId(systemId);

            JsonPatientIdentifier json = new JsonPatientIdentifier();
            json.setServiceId(serviceId);
            json.setServiceName(serviceName);
            json.setSystemId(systemId);
            json.setSystemName(systemName);
            json.setNhsNumber(identifier.getNhsNumber());
            json.setForenames(identifier.getForenames());
            json.setSurname(identifier.getSurname());
            json.setDateOfBirth(identifier.getDateOfBirth());
            json.setPostcode(identifier.getPostcode());
            json.setGender(identifier.getGender().getDisplay());
            json.setPatientId(identifier.getPatientId());
            json.setLocalId(identifier.getLocalId());
            json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);

        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static String getServiceNameForId(UUID serviceId) {
        Service service = serviceRepository.getById(serviceId);
        return service.getName();

    }
    private static String getSystemNameForId(UUID systemId) {
        ActiveItem activeItem = libraryRepository.getActiveItemByItemId(systemId);
        Item item = libraryRepository.getItemByKey(systemId, activeItem.getAuditId());
        return item.getTitle();
    }
}

