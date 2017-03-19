package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.AbstractCsvWriter;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.reverseCsv.schema.admin.Location;
import org.endeavourhealth.transform.emis.reverseCsv.schema.admin.Patient;
import org.endeavourhealth.transform.emis.reverseCsv.schema.appointment.Session;
import org.endeavourhealth.transform.emis.reverseCsv.transforms.AbstractTransformer;
import org.endeavourhealth.transform.emis.reverseCsv.transforms.FhirLocationTransformer;
import org.endeavourhealth.transform.emis.reverseCsv.transforms.FhirPatientTransformer;
import org.endeavourhealth.transform.emis.reverseCsv.transforms.FhirScheduleTransformer;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FhirToEmisCsvTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(FhirToEmisCsvTransformer.class);

    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"; //EMIS spec says "dd/MM/yyyy", but test data is different
    public static final String TIME_FORMAT = "hh:mm:ss";

    public static String transformFromFhir(UUID serviceId,
                                           UUID orgId,
                                           UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        List<ResourceByExchangeBatch> filteredResources = filterResources(resourcesByExchangeBatch, resourceIds);

        //create the CSV writers, to generate all the output files
        Map<Class, AbstractCsvWriter> writers = createWriters(CSV_FORMAT, DATE_FORMAT_YYYY_MM_DD, TIME_FORMAT);

        tranformResources(filteredResources, orgId, writers);


        //may as well zip the data, since it will compress well
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        //close each writer, and write its bytes to the zip file
        for (AbstractCsvWriter writer: writers.values()) {

            byte[] bytes = writer.close();
            String name = writer.getFileName();

            zos.putNextEntry(new ZipEntry(name));
            zos.write(bytes);
        }

        zos.flush();
        zos.close();

        //return as base64 encoded string
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static Map<Class, AbstractCsvWriter> createWriters(CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        List<AbstractCsvWriter> writers = new ArrayList<>();
        writers.add(new Location("Admin_Location.csv", csvFormat, dateFormat, timeFormat));
        writers.add(new Session("Appointment_Session.csv", csvFormat, dateFormat, timeFormat));
        writers.add(new Patient("Admin_Patient.csv", csvFormat, dateFormat, timeFormat));

        //hash the writers by class, for faster lookup later
        Map<Class, AbstractCsvWriter> ret = new HashMap<>();

        for (AbstractCsvWriter writer: writers) {
            ret.put(writer.getClass(), writer);
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

    private static void tranformResources(List<ResourceByExchangeBatch> resources, UUID orgId, Map<Class, AbstractCsvWriter> writers) throws Exception {

        //hash the resources by reference to them, so we can process in a specific order
        Map<String, ResourceByExchangeBatch> resourcesMap = hashResourcesByReference(resources);



        Organisation org = new OrganisationRepository().getById(orgId);
        String orgNationalId = org.getNationalId();

        //we detect whether we're doing an update or insert, based on whether we're previously mapped
        //a reference to a resource, so we need to transform the resources in a specific order, so
        //that we transform resources before we ones that refer to them
//        tranformResources(ResourceType.Organization, new OrganisationTransformer(orgNationalId), data, resources, resourcesMap, null);

        //if this is the first time processing this organisation's data, we will have generated the enterprise ID for that org while transforming the orgs
        //Integer enterpriseOrganisationUuid = new EnterpriseIdMapRepository().getEnterpriseOrganisationIdMapping(orgNationalId);
        Integer enterpriseOrganisationUuid =null;

                tranformResources(ResourceType.Location, new FhirLocationTransformer(), writers, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Practitioner, new PractitionerTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Schedule, new FhirScheduleTransformer(), writers, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.EpisodeOfCare, new EpisodeOfCareTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
        tranformResources(ResourceType.Patient, new FhirPatientTransformer(), writers, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Appointment, new AppointmentTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Encounter, new EncounterTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Condition, new ConditionTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Procedure, new ProcedureTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.ReferralRequest, new ReferralRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.ProcedureRequest, new ProcedureRequestTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Observation, new ObservationTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.MedicationStatement, new MedicationStatementTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.MedicationOrder, new MedicationOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Immunization, new ImmunisationTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.FamilyMemberHistory, new FamilyMemberHistoryTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.AllergyIntolerance, new AllergyIntoleranceTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.DiagnosticOrder, new DiagnosticOrderTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.DiagnosticReport, new DiagnosticReportTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.Specimen, new SpecimenTransformer(), data, resources, resourcesMap, enterpriseOrganisationUuid);

        //for these resource types, call with a null transformer as they're actually transformed when
        //doing one of the above entities, but we want to remove them from the resources list
//        tranformResources(ResourceType.Slot, null, data, resources, resourcesMap, enterpriseOrganisationUuid);
//        tranformResources(ResourceType.EpisodeOfCare, null, data, resources, resourcesMap, enterpriseOrganisationUuid);

        //if there's anything left in the list, then we've missed a resource type
        if (!resources.isEmpty()) {
            Set<String> resourceTypesMissed = new HashSet<>();
            for (ResourceByExchangeBatch resource: resources) {
                String resourceType = resource.getResourceType();
                if (!resourceTypesMissed.contains(resource)) {
                    LOG.error("Reverse Transform to Emis CSV doesn't handle {} resource types", resourceType);
                    resourceTypesMissed.add(resourceType);
                }
            }
        }

    }

    private static void tranformResources(ResourceType resourceType,
                                          AbstractTransformer transformer,
                                          Map<Class, AbstractCsvWriter> writers,
                                          List<ResourceByExchangeBatch> resources,
                                          Map<String, ResourceByExchangeBatch> resourcesMap,
                                          Integer enterpriseOrganisationId) throws Exception {

        for (int i=resources.size()-1; i>=0; i--) {
            ResourceByExchangeBatch resource = resources.get(i);
            if (resource.getResourceType().equals(resourceType.toString())) {

                //we use this function with a null transformer for resources we want to ignore
                if (transformer != null) {
                    try {
                        transformer.transform(resource, writers);
                        //transformer.transform(resource, writers, resourcesMap, enterpriseOrganisationId);
                    } catch (Exception ex) {
                        throw new TransformException("Exception transforming " + resourceType + " " + resource.getResourceId(), ex);
                    }

                }

                resources.remove(i);
            }
        }
    }
}
