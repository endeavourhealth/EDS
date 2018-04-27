package org.endeavourhealth.messagingapi.endpoints;

import com.google.common.base.Strings;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/subscriber")
public class SubscriberApi {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApi.class);

    private static final String FRAILTY_CODE = "289999999105";
    private static final String SUBSCRBER_SYSTEM_NAME = "???"; //TODO - need to find System Name again

    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"eds_read", "eds_read_only", "eds_read_write"})
    public Response uploadFiles(@Context HttpServletRequest request,
                                @Context UriInfo uriInfo,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode) throws Exception{

        LOG.info("Subscriber API request received with resource type = [" + resourceTypeRequested + "] and ODS code [" + headerOdsCode + "]");

        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        String subject = null;
        String code = null;

        for (String key: params.keySet()) {
            String value = params.getFirst(key);
            LOG.info("Request parameter [" + key + "[ = [" + value + "]");

            if (key.equalsIgnoreCase("subject")) {
                subject = value;

            } else if (key.equalsIgnoreCase("code")) {
                code = value;

            } else {
                throw new Exception("Invalid parameter '" + key + "'");
            }
        }

        //validate all expected parameters and headers are there
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
            throw new Exception("Only flag FHIR resource types can be requested");
        }

        if (!code.equalsIgnoreCase(FRAILTY_CODE)) {
            throw new Exception("Only code " + FRAILTY_CODE + " can be requested");
        }

        //TODO - the ODS code of the requesting 111 service is passed as a parameter - we need to validate that the requesting user can request on behalf of that service

        //find the service the request is being made for
        ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
        org.endeavourhealth.core.database.dal.admin.models.Service requestingService = serviceDalI.getByLocalIdentifier(headerOdsCode);
        if (requestingService == null) {
            throw new Exception("Unknown requesting ODS code '" + headerOdsCode + "'");
        }
        UUID serviceId = requestingService.getId();

        UUID systemId = SystemHelper.findSystemUuid(requestingService, SUBSCRBER_SYSTEM_NAME);
        if (systemId == null) {
            throw new Exception("Requesting organisation not configured for " + SUBSCRBER_SYSTEM_NAME);
        }

        //ensure the service is a valid subscriber to at least one protocol
        List<Protocol> protocols = getProtocolsForSubscriberService(serviceId.toString(), systemId.toString());
        if (protocols.isEmpty()) {
            throw new Exception("No valid subscriber agreement found for requesting ODS code '" + headerOdsCode + "'");
        }

        //find patient

        //ensure patient ORG is a publisher to that protocol



        //code =
        //subject
        //resource type
        //ods code


        //find patient in Discovery
        //if no patient, return an error
        //get patient owning organisation(s)
        //validate protocol exists between requester and owning organisation
        //return FHIR resource
        //change exceptions to property ones with proper error codes


        return null;
    }

    private static List<Protocol> getProtocolsForSubscriberService(String serviceUuid, String systemUuid) throws PipelineException {

        try {
            List<Protocol> ret = new ArrayList<>();

            List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, systemUuid);

            //the above fn will return is all protocols where the service and system are present, but we want to filter
            //that down to only ones where our service and system are an active publisher
            for (LibraryItem libraryItem: libraryItems) {
                Protocol protocol = libraryItem.getProtocol();
                if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

                    for (ServiceContract serviceContract : protocol.getServiceContract()) {
                        if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                                && serviceContract.getService().getUuid().equals(serviceUuid)
                                && serviceContract.getSystem().getUuid().equals(systemUuid)
                                && serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

                            ret.add(protocol);
                            break;
                        }
                    }
                }
            }

            return ret;

        } catch (Exception ex) {
            throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
        }
    }


}
