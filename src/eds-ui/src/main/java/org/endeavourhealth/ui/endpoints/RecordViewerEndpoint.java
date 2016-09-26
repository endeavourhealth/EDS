package org.endeavourhealth.ui.endpoints;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.business.recordViewer.RecordViewerBusiness;
import org.endeavourhealth.ui.business.recordViewer.models.JsonEncounter;
import org.endeavourhealth.ui.business.recordViewer.models.JsonPatient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/recordViewer")
public final class RecordViewerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerEndpoint.class);
    private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/findPatient")
    public Response findPatient(@Context SecurityContext sc,
                                    @QueryParam("searchTerms") String searchTerms) throws Exception {

        List<JsonPatient> result = new ArrayList<>();

        List<PatientIdentifierByLocalId> patientIds = identifierRepository.getFivePatients();

        for (PatientIdentifierByLocalId patientId : patientIds)
            result.add(RecordViewerBusiness.getPatient(patientId.getServiceId(), patientId.getSystemId(), patientId.getPatientId()));

        return Response
                .ok()
                .entity(result)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getPatient")
    public Response getPatient(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        Validate.notNull(serviceId, "serviceId");
        Validate.notNull(systemId, "systemId");
        Validate.notNull(patientId, "patientId");

        JsonPatient patient = RecordViewerBusiness.getPatient(serviceId, systemId, patientId);

        return Response
                .ok()
                .entity(patient)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEncounters")
    public Response getEncounters(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        Validate.notNull(serviceId, "serviceId");
        Validate.notNull(systemId, "systemId");
        Validate.notNull(patientId, "patientId");

        List<JsonEncounter> encounters = RecordViewerBusiness.getEncounters(serviceId, systemId, patientId);

        return Response
                .ok()
                .entity(encounters)
                .build();
    }
}