package org.endeavourhealth.transform.enterprise;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.FhirToXTransformerBase;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.enterprise.transforms.*;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;

public class FhirToEnterpriseCsvTransformer extends FhirToXTransformerBase {

    private static final Logger LOG = LoggerFactory.getLogger(FhirToEnterpriseCsvTransformer.class);

    //private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static String transformFromFhir(UUID senderOrganisationUuid,
                                           UUID batchId,
                                           Map<ResourceType,
                                           List<UUID>> resourceIds,
                                           boolean pseudonymised,
                                           String configName) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> filteredResources = getResources(batchId, resourceIds);
        if (filteredResources.isEmpty()) {
            return null;
        }

        LOG.trace("Transforming batch " + batchId + " and " + filteredResources.size() + " resources for " + configName);

        //we need to find the sender organisation national ID for the data in the batch
        Organisation org = new OrganisationRepository().getById(senderOrganisationUuid);
        String orgNationalId = org.getNationalId();

        try {
            OutputContainer data = tranformResources(filteredResources, orgNationalId, pseudonymised, configName);

            byte[] bytes = data.writeToZip();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (Exception ex) {
            throw new TransformException("Exception transforming batch " + batchId, ex);
        }
    }

    /**
     * all resources in a batch are for the same patient (or no patient at all), so rather than looking
     * up the Enterprise patient ID for each resource, we can do it once at the start. To do that
     * we need the Discovery patient ID from one of the resources.
     */
    private static String findPatientId(List<ResourceByExchangeBatch> resourceWrappers) throws Exception {

        for (ResourceByExchangeBatch resourceWrapper: resourceWrappers) {
            if (resourceWrapper.getIsDeleted()) {
                continue;
            }

            String resourceTypeStr = resourceWrapper.getResourceType();
            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);
            if (!FhirResourceFiler.isPatientResource(resourceType)) {
                continue;
            }

            Resource resource = AbstractTransformer.deserialiseResouce(resourceWrapper);
            String patientId = IdHelper.getPatientId(resource);
            if (Strings.isNullOrEmpty(patientId)) {
                continue;
            }

            return patientId;
        }

        return null;
    }


    private static OutputContainer tranformResources(List<ResourceByExchangeBatch> resources, String orgNationalId, 
                                                     boolean pseudonymised, String configName) throws Exception {

        //hash the resources by reference to them, so the transforms can quickly look up dependant resources
        Map<String, ResourceByExchangeBatch> resourcesMap = hashResourcesByReference(resources);

        OutputContainer data = new OutputContainer(pseudonymised);

        //we detect whether we're doing an update or insert, based on whether we're previously mapped
        //a reference to a resource, so we need to transform the resources in a specific order, so
        //that we transform resources before we ones that refer to them
        tranformResources(ResourceType.Organization, new OrganisationTransformer(orgNationalId), data, resources, resourcesMap, null, null, null, configName);

        //if this is the first time processing this organisation's data, we will have generated the enterprise ID for that org while transforming the orgs
        Long enterpriseOrganisationId = EnterpriseIdHelper.findEnterpriseOrganisationId(orgNationalId);
        if (enterpriseOrganisationId == null) {
            throw new TransformException("Failed to find enterprise ID for org natioanl ID " + orgNationalId);
        }
        /*Integer enterpriseOrganisationUuid = new EnterpriseIdMapRepository().getEnterpriseOrganisationIdMapping(orgNationalId);
        if (enterpriseOrganisationUuid == null) {
            throw new TransformException("Failed to find enterprise ID for org natioanl ID " + orgNationalId);
        }*/

        tranformResources(ResourceType.Practitioner, new PractitionerTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, null, null, configName);
        tranformResources(ResourceType.Schedule, new ScheduleTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, null, null, configName);
        tranformResources(ResourceType.Patient, new PatientTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, null, null, configName);

        //having done any patient resource, we should have created an enterprise patient ID and person ID that we can use for all remaining resources
        Long enterprisePatientId = null;
        Long enterprisePersonId = null;
        String discoveryPatientId = findPatientId(resources);
        if (!Strings.isNullOrEmpty(discoveryPatientId)) {
            enterprisePatientId = AbstractTransformer.findEnterpriseId(data.getPatients(), ResourceType.Patient.toString(), discoveryPatientId);
            if (enterprisePatientId == null) {
                throw new TransformException("No enterprise patient ID found for discovery patient " + discoveryPatientId);
            }

            String discoveryPersonId = PatientLinkHelper.getPersonId(discoveryPatientId);

            //if we've got some cases where we've got a deleted patient but non-deleted patient-related resources
            //all in the same batch, because Emis sent it like that. In that case we won't have a person ID, so
            //return out without processing any of the remaining resources, since they're for a deleted patient.
            if (Strings.isNullOrEmpty(discoveryPersonId)) {
                return data;
            }

            enterprisePersonId = EnterpriseIdHelper.findOrCreateEnterprisePersonId(discoveryPersonId, configName);
        }

        tranformResources(ResourceType.EpisodeOfCare, new EpisodeOfCareTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Appointment, new AppointmentTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Encounter, new EncounterTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Condition, new ConditionTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Procedure, new ProcedureTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.ReferralRequest, new ReferralRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.ProcedureRequest, new ProcedureRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Observation, new ObservationTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.MedicationStatement, new MedicationStatementTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.MedicationOrder, new MedicationOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Immunization, new ImmunisationTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.FamilyMemberHistory, new FamilyMemberHistoryTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.AllergyIntolerance, new AllergyIntoleranceTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.DiagnosticOrder, new DiagnosticOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.DiagnosticReport, new DiagnosticReportTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Specimen, new SpecimenTransformer(), data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);

        //for these resource types, call with a null transformer as they're actually transformed when
        //doing one of the above entities, but we want to remove them from the resources list
        tranformResources(ResourceType.Slot, null, data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
        tranformResources(ResourceType.Location, null, data, resources, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);

        //if there's anything left in the list, then we've missed a resource type
        if (!resources.isEmpty()) {
            Set<String> resourceTypesMissed = new HashSet<>();
            for (ResourceByExchangeBatch resource: resources) {
                String resourceType = resource.getResourceType();
                if (!resourceTypesMissed.contains(resource)) {
                    LOG.error("Transform to Enterprise doesn't handle {} resource types", resourceType);
                    resourceTypesMissed.add(resourceType);
                }
            }
        }

        return data;
    }

    private static void tranformResources(ResourceType resourceType,
                                          AbstractTransformer transformer,
                                          OutputContainer data,
                                          List<ResourceByExchangeBatch> resources,
                                          Map<String, ResourceByExchangeBatch> resourcesMap,
                                          Long enterpriseOrganisationId,
                                          Long enterprisePatientId,
                                          Long enterprisePersonId,
                                          String configName) throws Exception {

        //find all the ones we want to transform
        List<ResourceByExchangeBatch> resourcesToTransform = new ArrayList<>();
        HashSet<ResourceByExchangeBatch> hsResourcesToTransform = new HashSet<>();
        for (ResourceByExchangeBatch resource: resources) {
            if (resource.getResourceType().equals(resourceType.toString())) {
                resourcesToTransform.add(resource);
                hsResourcesToTransform.add(resource);
            }
        }

        if (resourcesToTransform.isEmpty()) {
            return;
        }

        //remove all the resources we processed, so we can check for ones we missed at the end
        //removeAll is really slow, so changing around
        for (int i=resources.size()-1; i>=0; i--) {
            ResourceByExchangeBatch r = resources.get(i);
            if (hsResourcesToTransform.contains(r)) {
                resources.remove(i);
            }
        }
        //resources.removeAll(resourcesToTransform);

        //we use this function with a null transformer for resources we want to ignore
        if (transformer != null) {
            int threads = Math.min(10, resources.size()/10); //limit to 10 threads, but don't create too many unnecessarily if we only have a few resources
            threads = Math.max(threads, 1); //make sure we have a min of 1

            ThreadPool threadPool = new ThreadPool(threads, 1000);

            for (ResourceByExchangeBatch resource: resourcesToTransform) {

                TransformResourceCallable callable = new TransformResourceCallable(transformer, resource, data,
                                                                        resourcesMap, enterpriseOrganisationId,
                                                                        enterprisePatientId, enterprisePersonId, configName);
                List<ThreadPoolError> errors = threadPool.submit(callable);
                handleErrors(errors);
            }

            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);
        }

    }

    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        Exception exception = first.getException();
        throw exception;
    }



    /*private static void tranformResources(ResourceType resourceType,
                                          AbstractTransformer transformer,
                                          OutputContainer data,
                                          List<ResourceByExchangeBatch> resources,
                                          Map<String, ResourceByExchangeBatch> resourcesMap,
                                          Long enterpriseOrganisationId,
                                          Long enterprisePatientId,
                                          Long enterprisePersonId,
                                          String configName) throws Exception {

        HashSet<ResourceByExchangeBatch> resourcesProcessed = new HashSet<>();

        *//*for (int i=resources.size()-1; i>=0; i--) {
            ResourceByExchangeBatch resource = resources.get(i);*//*
        for (ResourceByExchangeBatch resource: resources) {
            if (resource.getResourceType().equals(resourceType.toString())) {

                //we use this function with a null transformer for resources we want to ignore
                if (transformer != null) {
                    try {
                        transformer.transform(resource, data, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
                    } catch (Exception ex) {
                        throw new TransformException("Exception transforming " + resourceType + " " + resource.getResourceId(), ex);
                    }

                }

                resourcesProcessed.add(resource);
                //resources.remove(i);
            }
        }

        //remove all the resources we processed, so we can check for ones we missed at the end
        resources.removeAll(resourcesProcessed);
    }*/

    /**
     * hashes the resources by a reference to them, so the transforms can quickly look up dependant resources
     */
    private static Map<String, ResourceByExchangeBatch> hashResourcesByReference(List<ResourceByExchangeBatch> allResources) throws Exception {

        Map<String, ResourceByExchangeBatch> ret = new HashMap<>();

        for (ResourceByExchangeBatch resource: allResources) {

            ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());
            String resourceId = resource.getResourceId().toString();

            Reference reference = ReferenceHelper.createReference(resourceType, resourceId);
            String referenceStr = reference.getReference();
            ret.put(referenceStr, resource);
        }

        return ret;
    }

    static class TransformResourceCallable implements Callable {

        private AbstractTransformer transformer = null;
        private ResourceByExchangeBatch resource = null;
        private OutputContainer data = null;
        private Map<String, ResourceByExchangeBatch> resourcesMap = null;
        private Long enterpriseOrganisationId = null;
        private Long enterprisePatientId = null;
        private Long enterprisePersonId = null;
        private String configName = null;

        public TransformResourceCallable(AbstractTransformer transformer,
                                         ResourceByExchangeBatch resource,
                                         OutputContainer data,
                                         Map<String, ResourceByExchangeBatch> resourcesMap,
                                         Long enterpriseOrganisationId,
                                         Long enterprisePatientId,
                                         Long enterprisePersonId,
                                         String configName) {

            this.transformer = transformer;
            this.resource = resource;
            this.data = data;
            this.resourcesMap = resourcesMap;
            this.enterpriseOrganisationId = enterpriseOrganisationId;
            this.enterprisePatientId = enterprisePatientId;
            this.enterprisePersonId = enterprisePersonId;
            this.configName = configName;
        }

        @Override
        public Object call() throws Exception {
            try {
                transformer.transform(resource, data, resourcesMap, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);
            } catch (Exception ex) {
                throw new TransformException("Exception transforming " + resource.getResourceType() + " " + resource.getResourceId(), ex);
            }
            return null;
        }
    }

}
