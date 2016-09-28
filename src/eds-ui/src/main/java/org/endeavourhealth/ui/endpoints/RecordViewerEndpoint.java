package org.endeavourhealth.ui.endpoints;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.endeavourhealth.transform.ui.transforms.UITransform;
import org.endeavourhealth.transform.ui.models.UIPatient;
import org.endeavourhealth.ui.utility.ResourceFetcher;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
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

        List<UIPatient> result = new ArrayList<>();

        List<PatientIdentifierByLocalId> patientIds = identifierRepository.getFivePatients();

        for (PatientIdentifierByLocalId patientId : patientIds)
            result.add(getPatient(patientId.getServiceId(), patientId.getSystemId(), patientId.getPatientId()));

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

        UIPatient patient = getPatient(serviceId, systemId, patientId);

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

        List<Encounter> fhirEncounters = ResourceFetcher.getResourceByPatient(serviceId, systemId, patientId, Encounter.class);

        List<UUID> practitionerIds = UITransform.getPractitionerIds(fhirEncounters);

        List<Practitioner> fhirPractitioners = ResourceFetcher.getResourcesByService(serviceId, systemId, practitionerIds, Practitioner.class);

        List<UIEncounter> encounters = UITransform.transformEncounters(fhirEncounters, fhirPractitioners);

        return Response
                .ok()
                .entity(encounters)
                .build();
    }

    private static UIPatient getPatient(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = ResourceFetcher.getSingleResourceByPatient(serviceId, systemId, patientId, Patient.class);

        return UITransform.transformPatient(patient)
                .setServiceId(serviceId)
                .setSystemId(systemId)
                .setPatientId(patientId);
    }
}