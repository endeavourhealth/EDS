package org.endeavourhealth.transform.enterprise;

import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.transform.common.FhirToXTransformerBase;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.enterprise.transforms.*;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FhirToEnterpriseCsvTransformer extends FhirToXTransformerBase {

    private static final Logger LOG = LoggerFactory.getLogger(FhirToEnterpriseCsvTransformer.class);

    //private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static String transformFromFhir(UUID senderOrganisationUuid,
                                           UUID batchId,
                                           Map<ResourceType,
                                           List<UUID>> resourceIds,
                                           boolean pseudonymised) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> filteredResources = getResources(batchId, resourceIds);
        if (filteredResources.isEmpty()) {
            return null;
        }

        //we need to find the sender organisation national ID for the data in the batch
        Organisation org = new OrganisationRepository().getById(senderOrganisationUuid);
        String orgNationalId = org.getNationalId();

        try {
            OutputContainer data = tranformResources(filteredResources, orgNationalId, pseudonymised);

            byte[] bytes = data.writeToZip();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (Exception ex) {
            throw new TransformException("Exception transforming batch " + batchId, ex);
        }
    }

    private static OutputContainer tranformResources(List<ResourceByExchangeBatch> resources, String orgNationalId, boolean pseudonymised) throws Exception {

        //hash the resources by eference to them, so the transforms can quickly look up dependant resources
        Map<String, ResourceByExchangeBatch> resourcesMap = hashResourcesByReference(resources);

        OutputContainer data = new OutputContainer(pseudonymised);

        //we detect whether we're doing an update or insert, based on whether we're previously mapped
        //a reference to a resource, so we need to transform the resources in a specific order, so
        //that we transform resources before we ones that refer to them
        tranformResources(ResourceType.Organization, new OrganisationTransformer(orgNationalId), data, resources, resourcesMap, null);

        //if this is the first time processing this organisation's data, we will have generated the enterprise ID for that org while transforming the orgs
        Long enterpriseOrganisationUuid = EnterpriseIdHelper.findEnterpriseOrganisationId(orgNationalId);
        if (enterpriseOrganisationUuid == null) {
            throw new TransformException("Failed to find enterprise ID for org natioanl ID " + orgNationalId);
        }
        /*Integer enterpriseOrganisationUuid = new EnterpriseIdMapRepository().getEnterpriseOrganisationIdMapping(orgNationalId);
        if (enterpriseOrganisationUuid == null) {
            throw new TransformException("Failed to find enterprise ID for org natioanl ID " + orgNationalId);
        }*/

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
                                          Long enterpriseOrganisationId) throws Exception {

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



}
