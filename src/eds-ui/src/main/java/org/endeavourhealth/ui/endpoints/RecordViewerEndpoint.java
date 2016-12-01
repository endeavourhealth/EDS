package org.endeavourhealth.ui.endpoints;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.ehr.PatientIdentifierRepository;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.clinicial.*;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UIInternalIdentifier;
import org.endeavourhealth.transform.ui.models.types.UIService;
import org.endeavourhealth.transform.ui.transforms.clinical.UIClinicalTransform;
import org.endeavourhealth.transform.ui.transforms.UITransform;
import org.endeavourhealth.ui.utility.ResourceFetcher;
import org.endeavourhealth.ui.utility.SearchTermsParser;
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
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getServices")
    public Response getServices(@Context SecurityContext sc) throws Exception {

        List<UIService> uiServices = UITransform.transformServices(Lists.newArrayList(serviceRepository.getAll()));

        return buildResponse(uiServices);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/findPatient")
    public Response findPatient(@Context SecurityContext sc,
                                @QueryParam("serviceId") UUID serviceId,
                                @QueryParam("systemId") UUID systemId,
                                @QueryParam("searchTerms") String searchTerms) throws Exception {

        List<UIPatient> result = new ArrayList<>();

        if (!StringUtils.isEmpty(searchTerms)) {

            SearchTermsParser parser = new SearchTermsParser(searchTerms);

            List<PatientIdentifierByLocalId> patientsFound = new ArrayList<>();

            if (parser.hasNhsNumber())
                 patientsFound.addAll(identifierRepository.getForNhsNumberTemporary(serviceId, systemId, parser.getNhsNumber()));

            if (parser.hasEmisNumber())
                patientsFound.addAll(identifierRepository.getForLocalId(serviceId, systemId, parser.getEmisNumber()));

            for (String name : parser.getNames()) {
                if (StringUtils.isNotBlank(name)) {
                    patientsFound.addAll(identifierRepository.getForForenamesTemporary(serviceId, systemId, name));
                    patientsFound.addAll(identifierRepository.getForSurnameTemporary(serviceId, systemId, name));
                }
            }

            List<UIInternalIdentifier> uiInternalIdentifiers = patientsFound
                    .stream()
                    .map(t -> new UIInternalIdentifier()
                                .setServiceId(t.getServiceId())
                                .setSystemId(t.getSystemId())
                                .setResourceId(t.getPatientId()))
                    .distinct()
                    .collect(Collectors.toList());

            for (UIInternalIdentifier identifier : uiInternalIdentifiers)
                result.add(getPatient(identifier.getServiceId(), identifier.getSystemId(), identifier.getResourceId()));
        }

        return buildResponse(result);
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
        return buildResponse(patient);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getMedicationOrders")
    public Response getMedicationOrders(@Context SecurityContext sc,
                                        @QueryParam("serviceId") UUID serviceId,
                                        @QueryParam("systemId") UUID systemId,
                                        @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, MedicationOrder.class, UIMedicationOrder.class);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getConditions")
    public Response getConditions(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, Condition.class, UICondition.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getProblems")
    public Response getProblems(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, Condition.class, UIProblem.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEncounters")
    public Response getEncounters(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, Encounter.class, UIEncounter.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getDiary")
    public Response getDiary(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, ProcedureRequest.class, UIDiary.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getObservations")
    public Response getObservations(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, Observation.class, UIObservation.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllergies")
    public Response getAllergies(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, AllergyIntolerance.class, UIAllergyIntolerance.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getImmunizations")
    public Response getImmunizations(@Context SecurityContext sc,
                                 @QueryParam("serviceId") UUID serviceId,
                                 @QueryParam("systemId") UUID systemId,
                                 @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, Immunization.class, UIImmunisation.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getFamilyHistory")
    public Response getFamilyHistory(@Context SecurityContext sc,
                                     @QueryParam("serviceId") UUID serviceId,
                                     @QueryParam("systemId") UUID systemId,
                                     @QueryParam("patientId") UUID patientId) throws Exception {

        return getClinicalResourceResponse(serviceId, systemId, patientId, FamilyMemberHistory.class, UIFamilyMemberHistory.class);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void validateIdentifiers(UUID serviceId, UUID systemId, UUID patientId) {
        Validate.notNull(serviceId, "serviceId");
        Validate.notNull(systemId, "systemId");
        Validate.notNull(patientId, "patientId");
    }

    private static UIPatient getPatient(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = ResourceFetcher.getSingleResourceByPatient(serviceId, systemId, patientId, Patient.class);
        UIPatient uiPatient = UITransform.transformPatient(patient);

        return uiPatient
                .setPatientId(new UIInternalIdentifier()
                    .setServiceId(serviceId)
                    .setSystemId(systemId)
                    .setResourceId(patientId));
    }

    private <T extends Resource,
            U extends UIResource> Response getClinicalResourceResponse(UUID serviceId,
                                                                       UUID systemId,
                                                                       UUID patientId,
                                                                       Class<T> fhirResourceType,
                                                                       Class<U> uiResourceType) throws Exception {
        validateIdentifiers(serviceId, systemId, patientId);

        List<U> encounters = getClinicalResources(serviceId, systemId, patientId, fhirResourceType, uiResourceType);

        return buildResponse(encounters);
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
        ReferencedResources referencedResources = getReferencedResources(serviceId, systemId, patientId, references);

        return transform.transform(resources, referencedResources);
    }

    private ReferencedResources getReferencedResources(UUID serviceId, UUID systemId, UUID patientId, List<Reference> references) throws Exception {

        ReferencedResources referencedResources = new ReferencedResources();

        List<UUID> practitionerIds = getIdsOfType(references, ResourceType.Practitioner);
        referencedResources.setPractitioners(ResourceFetcher.getResourcesByService(serviceId, systemId, practitionerIds, Practitioner.class));

        referencedResources.setMedications(ResourceFetcher.getResourceByPatient(serviceId, systemId, patientId, Medication.class));

        referencedResources.setObservations(ResourceFetcher.getResourceByPatient(serviceId, systemId, patientId, Observation.class), referencedResources);

        return referencedResources;
    }

    private static List<UUID> getIdsOfType(List<Reference> references, ResourceType resourceType) {
        return references
                .stream()
                .map(t -> ReferenceHelper.getReferenceId(t, resourceType))
                .filter(t -> StringUtils.isNotEmpty(t))
                .distinct()
                .map(t -> UUID.fromString(t.replace("{", "").replace("}", "")))
                .collect(Collectors.toList());
    }

    private Response buildResponse(Object entity) {
        return Response
                .ok()
                .entity(entity)
                .build();
    }
}