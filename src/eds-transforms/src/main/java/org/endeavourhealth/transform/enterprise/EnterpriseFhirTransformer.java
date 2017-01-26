package org.endeavourhealth.transform.enterprise;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.core.xml.EnterpriseSerializer;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.transforms.*;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EnterpriseFhirTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseFhirTransformer.class);

    private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static String transformFromFhir(UUID serviceId,
                                           UUID orgId,
                                           UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        List<ResourceByExchangeBatch> filteredResources = filterResources(resourcesByExchangeBatch, resourceIds);

        EnterpriseData data = tranformResources(filteredResources, orgId);

        //write our data to XML
        String xml = EnterpriseSerializer.writeToXml(data);

        //may as well zip the data, since it will compress well
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        zos.putNextEntry(new ZipEntry(ZIP_ENTRY));
        zos.write(xml.getBytes());

        zos.flush();
        zos.close();

        //return as base64 encoded string
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static EnterpriseData tranformResources(List<ResourceByExchangeBatch> resources, UUID orgId) throws Exception {

        //hash the resources by eference to them, so the transforms can quickly look up dependant resources
        Map<String, ResourceByExchangeBatch> resourcesMap = hashResourcesByReference(resources);

        EnterpriseData data = new EnterpriseData();

        Organisation org = new OrganisationRepository().getById(orgId);
        String orgNationalId = org.getNationalId();

        if (resources.size() > 0) {
            UUID batchId = resources.get(0).getBatchId();
            LOG.info("=============================================batch ID " + batchId + " org ID " + orgNationalId + " ========================================");
        }

        //we detect whether we're doing an update or insert, based on whether we're previously mapped
        //a reference to a resource, so we need to transform the resources in a specific order, so
        //that we transform resources before we ones that refer to them
        tranformResources(ResourceType.Organization, new OrganisationTransformer(orgNationalId), data, resources, resourcesMap, null);

        //if this is the first time processing this organisation's data, we will have generated the enterprise ID for that org while transforming the orgs
        Integer enterpriseOrganisationUuid = new EnterpriseIdMapRepository().getEnterpriseOrganisationIdMapping(orgNationalId);

        tranformResources(ResourceType.Practitioner, new PractitionerTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Schedule, new ScheduleTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Patient, new PatientTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.EpisodeOfCare, new EpisodeOfCareTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Appointment, new AppointmentTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Encounter, new EncounterTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Condition, new ConditionTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Procedure, new ProcedureTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.ReferralRequest, new ReferralRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.ProcedureRequest, new ProcedureRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Observation, new ObservationTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.MedicationStatement, new MedicationStatementTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.MedicationOrder, new MedicationOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Immunization, new ImmunisationTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.FamilyMemberHistory, new FamilyMemberHistoryTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.AllergyIntolerance, new AllergyIntoleranceTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.DiagnosticOrder, new DiagnosticOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.DiagnosticReport, new DiagnosticReportTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Specimen, new SpecimenTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);

        //for these resource types, call with a null transformer as they're actually transformed when
        //doing one of the above entities, but we want to remove them from the resources list
        tranformResources(ResourceType.Slot, null, data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Location, null, data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.EpisodeOfCare, null, data, resources, resourcesMap, enterpriseOrganisationUuid);

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
                                          EnterpriseData data,
                                          List<ResourceByExchangeBatch> resources,
                                          Map<String, ResourceByExchangeBatch> resourcesMap,
                                          Integer enterpriseOrganisationId) throws Exception {

        HashSet<ResourceByExchangeBatch> resourcesProcessed = new HashSet<>();

        /*for (int i=resources.size()-1; i>=0; i--) {
            ResourceByExchangeBatch resource = resources.get(i);*/
        for (ResourceByExchangeBatch resource: resources) {
            if (resource.getResourceType().equals(resourceType.toString())) {

                //we use this function with a null transformer for resources we want to ignore
                if (transformer != null) {
                    try {
                        transformer.transform(resource, data, resourcesMap, enterpriseOrganisationId);
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
    }

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


    private static List<ResourceByExchangeBatch> filterResources(List<ResourceByExchangeBatch> allResources,
                                                                 Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        List<ResourceByExchangeBatch> ret = new ArrayList<>();

        for (ResourceByExchangeBatch resource: allResources) {
            UUID resourceId = resource.getResourceId();
            ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());

            //the map of resource IDs tells us the resources that passed the protocol and should be passed
            //to the subscriber. However, any resources that should be deleted should be passed, whether the
            //protocol says to include it or not, since it may have previously been passed to the subscriber anyway
            if (resource.getIsDeleted()) {
                ret.add(resource);

            } else {

                //during testing, the resource ID is null, so handle this
                if (resourceIds == null) {
                    ret.add(resource);
                    continue;
                }

                List<UUID> uuidsToKeep = resourceIds.get(resourceType);
                if (uuidsToKeep != null
                        || uuidsToKeep.contains(resourceId)) {
                    ret.add(resource);
                }
            }
        }

        return ret;
    }
}
