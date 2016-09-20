package org.endeavourhealth.transform.enterprise;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.endeavourhealth.transform.enterprise.schema.ObjectFactory;
import org.endeavourhealth.transform.enterprise.transforms.*;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EnterpriseFhirTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseFhirTransformer.class);

    private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static String transformFromFhir(UUID serviceId,
                                           UUID orgNationalId,
                                           UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        List<ResourceByExchangeBatch> filteredResources = filterResources(resourcesByExchangeBatch, resourceIds);

        EnterpriseData data = tranformResources(filteredResources);

        //write our data to XML
        JAXBElement element = new ObjectFactory().createEnterpriseData(data);
        String xml = XmlSerializer.serializeToString(element, null);

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

    private static EnterpriseData tranformResources(List<ResourceByExchangeBatch> filteredResources) throws Exception {

        //hash the resources by reference to them, so we can process in a specific order
        Map<String, ResourceByExchangeBatch> filteredResourcesMap = hashResourcesByReference(filteredResources);

        //TODO - work out org ID
        UUID enterpriseOrganisationUuid = null;

        EnterpriseData data = new EnterpriseData();

        for (ResourceByExchangeBatch resource: filteredResources) {

            ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());

            if (resourceType == ResourceType.Patient) {
                PatientTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Condition) {
                ConditionTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Procedure) {
                ProcedureTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.ReferralRequest) {
                ReferralRequestTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.ProcedureRequest) {
                ProcedureRequestTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Schedule) {
                ScheduleTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Slot) {
                //slot resources are handled while we do Appointment resources

            } else if (resourceType == ResourceType.Practitioner) {
                PractitionerTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Observation) {
                ObservationTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Organization) {
                OrganisationTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.MedicationStatement) {
                MedicationStatementTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.MedicationOrder) {
                MedicationOrderTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Location) {
                //Locations are handled at the same time we handle Organisations

            } else if (resourceType == ResourceType.Immunization) {
                ImmunisationTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.FamilyMemberHistory) {
                FamilyMemberHistoryTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.EpisodeOfCare) {
                //EpisodeOfCare resources are handled at the same time as we handle Patients

            } else if (resourceType == ResourceType.Encounter) {
                EncounterTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.Appointment) {
                AppointmentTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.AllergyIntolerance) {
                AllergyIntoleranceTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else if (resourceType == ResourceType.DiagnosticOrder) {
                DiagnosticOrderTransformer.transform(resource, data, filteredResourcesMap, enterpriseOrganisationUuid);

            } else {
                throw new TransformException("Unsupported FHIR resource type " + resource.getResourceType());
            }

        }

        return data;
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


    private static void transformResource(ResourceByExchangeBatch resource,
                                          EnterpriseData data,
                                          Map<String, ResourceByExchangeBatch> allResources) throws Exception {


    }

    /*private static List<Resource> retrieveAllResources(UUID batchId) throws Exception {

        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        //LOG.info("Got {} resources for batch {}", resourcesByExchangeBatch.size(), batchId);

        List<Resource> ret = new ArrayList<>();

        for (ResourceByExchangeBatch resourceByExchangeBatch: resourcesByExchangeBatch) {
            String json = resourceByExchangeBatch.getResourceData();
            if (!Strings.isNullOrEmpty(json)) {
                try {
                    Resource r = new JsonParser().parse(json);
                    ret.add(r);
                    //LOG.info("Read " + r.getResourceType() + " ok");
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                    LOG.error(json);
                    throw ex;
                }
            }

        }

        return ret;
    }*/

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
