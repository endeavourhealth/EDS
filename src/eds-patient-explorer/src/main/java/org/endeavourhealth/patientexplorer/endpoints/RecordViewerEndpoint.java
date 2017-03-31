package org.endeavourhealth.patientexplorer.endpoints;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.rdbms.eds.PatientSearch;
import org.endeavourhealth.core.rdbms.eds.PatientSearchHelper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.utility.ResourceFetcher;
import org.endeavourhealth.patientexplorer.utility.SearchTermsParser;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.clinicial.*;
import org.endeavourhealth.transform.ui.models.types.UIInternalIdentifier;
import org.endeavourhealth.transform.ui.models.types.UIService;
import org.endeavourhealth.transform.ui.transforms.UITransform;
import org.endeavourhealth.transform.ui.transforms.clinical.UIClinicalTransform;
import org.hl7.fhir.instance.model.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Path("/recordViewer")
public final class RecordViewerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsPatientExplorerModule.RecordViewer);
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    //private static final PatientIdentifierRepository identifierRepository = new PatientIdentifierRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getServices")
    public Response getServices(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Service List");
        LOG.debug("getServices");

        List<UIService> uiServices = UITransform.transformServices(Lists.newArrayList(serviceRepository.getAll()));

        return buildResponse(uiServices);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/getServiceName")
    public Response getServiceName(@Context SecurityContext sc, @QueryParam("serviceId") UUID serviceId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Service Name",
            "ServiceId", serviceId);
        LOG.debug("getServiceName");

        List<Service> services = Lists.newArrayList(serviceRepository.getAll());
        Optional<Service> service = services.stream()
            .filter(s -> s.getId().equals(serviceId))
            .findFirst();

        if (service.isPresent())
            return buildResponse(service.get().getName());

        return buildResponse(null);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/findPerson")
    public Response findPerson(@Context SecurityContext sc, @QueryParam("searchTerms") String searchTerms) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Find person",
            "SearchTerm", searchTerms);
        LOG.debug("findPerson");

        Set<String> userServiceAccessList = SecurityUtils.getOrganisationRoles(sc).keySet();
        Map<String, UIPatient> result = new HashMap<>();


        if (!StringUtils.isEmpty(searchTerms)) {

            SearchTermsParser parser = new SearchTermsParser(searchTerms);

            List<PatientSearch> patientsFound = new ArrayList<>();

            if (parser.hasNhsNumber())
                patientsFound.addAll(PatientSearchHelper.searchByNhsNumber(userServiceAccessList, parser.getNhsNumber()));

            if (parser.hasEmisNumber())
                patientsFound.addAll(PatientSearchHelper.searchByLocalId(userServiceAccessList, parser.getEmisNumber()));

            if (parser.hasDateOfBirth())
                patientsFound.addAll(PatientSearchHelper.searchByDateOfBirth(userServiceAccessList, parser.getDateOfBirth()));


            patientsFound.addAll(PatientSearchHelper.searchByNames(userServiceAccessList, parser.getNames()));

            for (PatientSearch searchPatient : patientsFound) {
                if (!result.containsKey(searchPatient.getNhsNumber())) {
                	try {
										UIPatient patient = getPatient(
												UUID.fromString(searchPatient.getServiceId()),
												UUID.fromString(searchPatient.getSystemId()),
												UUID.fromString(searchPatient.getPatientId())
										);
										result.put(searchPatient.getNhsNumber(), patient);
									} catch (Exception e) {
                		LOG.error(e.getMessage(), searchPatient.getServiceId(), searchPatient.getSystemId(), searchPatient.getPatientId());
									}
                }
            }
        }

        return buildResponse(result.values());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/findPatient")
    public Response findPatient(@Context SecurityContext sc,
                                @QueryParam("serviceId") UUID serviceId,
                                @QueryParam("systemId") UUID systemId,
                                @QueryParam("searchTerms") String searchTerms) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Find patient",
                "SearchTerm", searchTerms,
                "ServiceId", serviceId,
                "SystemId", systemId);
        LOG.debug("findPatient");


        List<UIPatient> result = new ArrayList<>();

        if (!StringUtils.isEmpty(searchTerms)) {

            SearchTermsParser parser = new SearchTermsParser(searchTerms);

            List<PatientSearch> patientsFound = new ArrayList<>();

            if (parser.hasNhsNumber())
                patientsFound.addAll(PatientSearchHelper.searchByNhsNumber(serviceId, systemId, parser.getNhsNumber()));

            if (parser.hasEmisNumber())
                patientsFound.addAll(PatientSearchHelper.searchByLocalId(serviceId, systemId, parser.getEmisNumber()));

            if (parser.hasDateOfBirth())
                patientsFound.addAll(PatientSearchHelper.searchByDateOfBirth(serviceId, systemId, parser.getDateOfBirth()));


            patientsFound.addAll(PatientSearchHelper.searchByNames(serviceId, systemId, parser.getNames()));

            List<UIInternalIdentifier> uiInternalIdentifiers = patientsFound
                    .stream()
                    .map(t -> new UIInternalIdentifier()
                            .setServiceId(UUID.fromString(t.getServiceId()))
                            .setSystemId(UUID.fromString(t.getSystemId()))
                            .setResourceId(UUID.fromString(t.getPatientId())))
                    .distinct()
                    .collect(Collectors.toList());

            for (UIInternalIdentifier identifier : uiInternalIdentifiers)
                result.add(getPatient(identifier.getServiceId(), identifier.getSystemId(), identifier.getResourceId()));
        }

        return buildResponse(result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEpisodes")
    public Response getEpisodes(@Context SecurityContext sc,
                                @QueryParam("nhsNumber") String nhsNumber) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Episodes");
        LOG.debug("getEpisodes");

        // Get list of patients by NHS number
        List<PatientSearch> patientSearches = PatientSearchHelper.searchByNhsNumber(nhsNumber);
        List<UIEpisodeOfCare> episodes = new ArrayList<>();

        // Get episodes of care for each
        for(PatientSearch patientSearch : patientSearches) {
        	try {
						UIPatient patient = getPatient(
								UUID.fromString(patientSearch.getServiceId()),
								UUID.fromString(patientSearch.getSystemId()),
								UUID.fromString(patientSearch.getPatientId())
						);

						List<UIEpisodeOfCare> episodesOfCare = getClinicalResources(
								UUID.fromString(patientSearch.getServiceId()),
								UUID.fromString(patientSearch.getSystemId()),
								UUID.fromString(patientSearch.getPatientId()),
								EpisodeOfCare.class,
								UIEpisodeOfCare.class
						);

						episodesOfCare.forEach(episode -> episode.setPatient(patient));

						episodes.addAll(episodesOfCare);
					} catch (Exception e) {
        		LOG.error(e.getMessage(), patientSearch.getServiceId(), patientSearch.getSystemId(), patientSearch.getPatientId());
					}
        }

        return buildResponse(
            episodes.stream()
                .sorted(Comparator.comparing(f -> f.getPeriod().getStart().getDate()))
                .toArray()
        );

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getPatient")
    public Response getPatient(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Patient",
            "PatientId", patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getPatient");

        validateIdentifiers(serviceId, systemId, patientId);

        UIPatient patient = getPatient(serviceId, systemId, patientId);
        return buildResponse(patient);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getMedicationStatements")
    public Response getMedicationStatements(@Context SecurityContext sc,
                                        @QueryParam("serviceId") UUID serviceId,
                                        @QueryParam("systemId") UUID systemId,
                                        @QueryParam("patientId") UUID patientId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Medication Statements",
            "PatientId", patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getMedicationStatement");

        return getClinicalResourceResponse(serviceId, systemId, patientId, MedicationStatement.class, UIMedicationStatement.class);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getConditions")
    public Response getConditions(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Conditions",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getConditions");

        return getClinicalResourceResponse(serviceId, systemId, patientId, Condition.class, UICondition.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getProblems")
    public Response getProblems(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Problems",
            "PatientId", patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getProblems");

        return getClinicalResourceResponse(serviceId, systemId, patientId, Condition.class, UIProblem.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEncounters")
    public Response getEncounters(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Encounters",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getEncounters");

        return getClinicalResourceResponse(serviceId, systemId, patientId, Encounter.class, UIEncounter.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getDiary")
    public Response getDiary(@Context SecurityContext sc,
                                  @QueryParam("serviceId") UUID serviceId,
                                  @QueryParam("systemId") UUID systemId,
                                  @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Diaries",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getDiaries");

        return getClinicalResourceResponse(serviceId, systemId, patientId, ProcedureRequest.class, UIDiary.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getObservations")
    public Response getObservations(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Observations",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getObservations");

        return getClinicalResourceResponse(serviceId, systemId, patientId, Observation.class, UIObservation.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllergies")
    public Response getAllergies(@Context SecurityContext sc,
                                    @QueryParam("serviceId") UUID serviceId,
                                    @QueryParam("systemId") UUID systemId,
                                    @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Allergies",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getAllergies");

        return getClinicalResourceResponse(serviceId, systemId, patientId, AllergyIntolerance.class, UIAllergyIntolerance.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getImmunisations")
    public Response getImmunisations(@Context SecurityContext sc,
                                 @QueryParam("serviceId") UUID serviceId,
                                 @QueryParam("systemId") UUID systemId,
                                 @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Immunisations",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getImmunisations");

        return getClinicalResourceResponse(serviceId, systemId, patientId, Immunization.class, UIImmunisation.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getFamilyHistory")
    public Response getFamilyHistory(@Context SecurityContext sc,
                                     @QueryParam("serviceId") UUID serviceId,
                                     @QueryParam("systemId") UUID systemId,
                                     @QueryParam("patientId") UUID patientId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Family History",
            "PatientId",  patientId,
            "ServiceId", serviceId,
            "SystemId", systemId);
        LOG.debug("getFamilyHistory");

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

        List<UUID> organisationIds = getIdsOfType(references, ResourceType.Organization);
        referencedResources.setOrganisations(ResourceFetcher.getResourcesByService(serviceId, systemId, organisationIds, Organization.class));

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