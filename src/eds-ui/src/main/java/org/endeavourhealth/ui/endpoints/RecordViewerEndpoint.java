package org.endeavourhealth.ui.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UICondition;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIEncounter;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIProblem;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.transforms.clinical.UIClinicalTransform;
import org.endeavourhealth.transform.ui.transforms.UITransform;
import org.endeavourhealth.ui.utility.ResourceFetcher;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

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

        validateIdentifiers(serviceId, systemId, patientId);

        UIPatient patient = getPatient(serviceId, systemId, patientId);

        return Response
                .ok()
                .entity(patient)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getConditions")
    public Response getConditions(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        validateIdentifiers(serviceId, systemId, patientId);

        List<UICondition> conditions = getClinicalResources(serviceId, systemId, patientId, Condition.class, UICondition.class);

        return Response
                .ok()
                .entity(conditions)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getProblems")
    public Response getProblems(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        validateIdentifiers(serviceId, systemId, patientId);

        List<UIProblem> conditions = getClinicalResources(serviceId, systemId, patientId, Condition.class, UIProblem.class);

        return Response
                .ok()
                .entity(conditions)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEncounters")
    public Response getEncounters(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        validateIdentifiers(serviceId, systemId, patientId);

        List<UIEncounter> encounters = getClinicalResources(serviceId, systemId, patientId, Encounter.class, UIEncounter.class);

        // move to client
        encounters = encounters
                .stream()
                .sorted(Comparator.comparing(t -> ((UIEncounter)t).getPeriod().getStart()).reversed())
                .collect(Collectors.toList());

        return Response
                .ok()
                .entity(encounters)
                .build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void validateIdentifiers(UUID serviceId, UUID systemId, UUID patientId) {
        Validate.notNull(serviceId, "serviceId");
        Validate.notNull(systemId, "systemId");
        Validate.notNull(patientId, "patientId");
    }

    private static UIPatient getPatient(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = ResourceFetcher.getSingleResourceByPatient(serviceId, systemId, patientId, Patient.class);

        return UITransform.transformPatient(patient)
                .setServiceId(serviceId)
                .setSystemId(systemId)
                .setPatientId(patientId);
    }

    private <T extends Resource,
            U extends UIResource> List<U> getClinicalResources(UUID serviceId,
                                                               UUID systemId,
                                                               UUID patientId,
                                                               Class<T> fhirResourceType,
                                                               Class<U> uiResourceType) throws Exception {

        List<T> resources = ResourceFetcher.getResourceByPatient(serviceId, systemId, patientId, fhirResourceType);

        UIClinicalTransform transform = UITransform.getClinicalTransformer(uiResourceType);

        List<Reference> references = transform.getReferences(resources);
        ReferencedResources referencedResources = getReferencedResources(serviceId, systemId, references);

        return transform.transform(resources, referencedResources);
    }

    private ReferencedResources getReferencedResources(UUID serviceId, UUID systemId, List<Reference> references) throws Exception {

        ReferencedResources referencedResources = new ReferencedResources();

        List<UUID> practitionerIds = getIdsOfType(references, ResourceType.Practitioner);
        referencedResources.setPractitioners(ResourceFetcher.getResourcesByService(serviceId, systemId, practitionerIds, Practitioner.class));

        return referencedResources;
    }

    private static List<UUID> getIdsOfType(List<Reference> references, ResourceType resourceType) {
        return references
                .stream()
                .map(t -> ReferenceHelper.getReferenceId(t, resourceType))
                .filter(t -> StringUtils.isNotEmpty(t))
                .distinct()
                .map(t -> UUID.fromString(t))
                .collect(Collectors.toList());
    }
}