package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.rdbms.eds.PatientSearch;
import org.endeavourhealth.core.rdbms.eds.PatientSearchHelper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.framework.exceptions.BadRequestException;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/patientIdentity")
@Metrics(registry = "EdsRegistry")
public final class PatientIdentityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(PatientIdentityEndpoint.class);

    //private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final LibraryRepository libraryRepository = new LibraryRepository();

    private UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.PatientIdentity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.PatientIdentityEndpoint.GetByLocalIdentifier")
    @Path("/byLocalIdentifier")
    public Response byLocalIdentifier(@Context SecurityContext sc,
                         @QueryParam("serviceId") String serviceIdStr,
                         @QueryParam("systemId") String systemIdStr,
                         @QueryParam("localId") String localId) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Patient By Local Id",
            "Service Id", serviceIdStr,
            "System Id", systemIdStr,
            "Local Id", localId);

        if (Strings.isNullOrEmpty(serviceIdStr)) {
            throw new BadRequestException("A service must be selected");
        }
        if (Strings.isNullOrEmpty(systemIdStr)) {
            throw new BadRequestException("A system must be selected");
        }
        if (Strings.isNullOrEmpty(localId)) {
            throw new BadRequestException("Local ID must be entered");
        }

        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);

        String serviceName = getServiceNameForId(serviceId);
        String systemName = getSystemNameForId(systemId);

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        List<PatientSearch> identifiers = PatientSearchHelper.searchByLocalId(serviceId, systemId, localId);
        for (PatientSearch identifier: identifiers) {

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
            json.setGender(identifier.getGender());
            json.setPatientId(UUID.fromString(identifier.getPatientId()));
            //json.setLocalId(identifier.getLocalId());
            //json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);
        }
        /*List<PatientIdentifierByLocalId> identifiers = identifierRepository.getForLocalId(serviceId, systemId, localId);
        for (PatientIdentifierByLocalId identifier: identifiers) {

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
        }*/

        removeDuplicates(ret);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.PatientIdentityEndpoint.GetByNHSNumber")
    @Path("/byNhsNumber")
    public Response byNhsNumber(@Context SecurityContext sc, @QueryParam("nhsNumber") String nhsNumber) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Patient By NHS Number",
            "NHS Number", nhsNumber);

        if (Strings.isNullOrEmpty(nhsNumber)) {
            throw new BadRequestException("NHS number must be entered");
        }

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        List<PatientSearch> identifiers = PatientSearchHelper.searchByNhsNumber(nhsNumber);
        for (PatientSearch identifier: identifiers) {

            UUID serviceId = UUID.fromString(identifier.getServiceId());
            UUID systemId = UUID.fromString(identifier.getSystemId());

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
            json.setGender(identifier.getGender());
            json.setPatientId(UUID.fromString(identifier.getPatientId()));
            //json.setLocalId(identifier.getLocalId());
            //json.setLocalIdSystem(identifier.getLocalIdSystem());

            ret.add(json);
        }
        /*List<PatientIdentifierByNhsNumber> identifiers = identifierRepository.getForNhsNumber(nhsNumber);
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
        }*/

        removeDuplicates(ret);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.PatientIdentityEndpoint.GetByPatientId")
    @Path("/byPatientId")
    public Response byPatientId(@Context SecurityContext sc, @QueryParam("patientId") String patientIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Patient",
            "Patient Id", patientIdStr);

        if (Strings.isNullOrEmpty(patientIdStr)) {
            throw new BadRequestException("Patient ID must be entered");
        }

        UUID patientId = null;
        try {
            patientId = UUID.fromString(patientIdStr);
        } catch (IllegalArgumentException ex) {
            //do nothing if it's not a valid UUID
        }

        List<JsonPatientIdentifier> ret = new ArrayList<>();

        if (patientId != null) {

            PatientSearch identifier = PatientSearchHelper.searchByPatientId(patientId);
            if (identifier != null) {

                UUID serviceId = UUID.fromString(identifier.getServiceId());
                UUID systemId = UUID.fromString(identifier.getSystemId());

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
                json.setGender(identifier.getGender());
                json.setPatientId(UUID.fromString(identifier.getPatientId()));
                //json.setLocalId(identifier.getLocalId());
                //json.setLocalIdSystem(identifier.getLocalIdSystem());

                ret.add(json);
            }
            /*PatientIdentifierByPatientId identifier = identifierRepository.getMostRecentByPatientId(patientId);
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
            }*/
        }

        removeDuplicates(ret);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    /**
     * because of the structure of the table in Cassandra, we may get duplicate rows, so just strip them out
     */
    private static void removeDuplicates(List<JsonPatientIdentifier> identifiers) {
        for (int i=identifiers.size()-1; i>=0; i--) {
            JsonPatientIdentifier identifier = identifiers.get(i);
            boolean duplicate = false;
            for (int j=i-1; j>=0; j--) {
                JsonPatientIdentifier otherIdentifier = identifiers.get(i);
                if (isSame(identifier, otherIdentifier)) {
                    duplicate = true;
                    break;
                }
            }

            if (duplicate) {
                identifiers.remove(i);
            }
        }
    }
    private static boolean isSame(JsonPatientIdentifier one, JsonPatientIdentifier two) {
        return isSame(one.getServiceId(), two.getServiceId())
                && isSame(one.getServiceName(), two.getServiceName())
                && isSame(one.getSystemId(), two.getSystemId())
                && isSame(one.getSystemName(), two.getSystemName())
                && isSame(one.getNhsNumber(), two.getNhsNumber())
                && isSame(one.getForenames(), two.getForenames())
                && isSame(one.getSurname(), two.getSurname())
                && isSame(one.getDateOfBirth(), two.getDateOfBirth())
                && isSame(one.getPostcode(), two.getGender())
                && isSame(one.getPatientId(), two.getPatientId())
                && isSame(one.getLocalId(), two.getLocalId())
                && isSame(one.getLocalIdSystem(), two.getLocalIdSystem());
    }
    private static boolean isSame(Date one, Date two) {
        if (one == null && two == null) {
            return true;
        } else if (one != null
                && two != null
                && one.equals(two)) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isSame(UUID one, UUID two) {
        if (one == null && two == null) {
            return true;
        } else if (one != null
                && two != null
                && one.equals(two)) {
            return true;
        } else {
            return false;
        }
    }
    private static boolean isSame(String one, String two) {
        if (one == null && two == null) {
            return true;
        } else if (one != null
                && two != null
                && one.equals(two)) {
            return true;
        } else {
            return false;
        }
    }

    private static String getServiceNameForId(UUID serviceId) {
        try {
            Service service = serviceRepository.getById(serviceId);
            return service.getName();
        } catch (NullPointerException ex ) {
            LOG.error("Failed to find service for ID " + serviceId, ex);
            return "UNKNOWN";
        }

    }
    private static String getSystemNameForId(UUID systemId) {
        try {
            ActiveItem activeItem = libraryRepository.getActiveItemByItemId(systemId);
            Item item = libraryRepository.getItemByKey(systemId, activeItem.getAuditId());
            return item.getTitle();
        } catch (NullPointerException ex) {
            LOG.error("Failed to find system for ID " + systemId, ex);
            return "UNKNOWN";
        }


    }
}

