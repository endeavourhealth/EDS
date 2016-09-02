package org.endeavourhealth.transform.ceg;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.transforms.*;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CegFhirTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(CegFhirTransformer.class);

    public static String transformFromFhir(UUID serviceId,
                                           UUID orgNationalId,
                                           UUID batchId,
                                           List<UUID> resourceIds) throws Exception {

        //retrieve our resources
        List<Resource> allResources = retrieveAllResources(batchId);
        List<Resource> filteredResources = filterResources(allResources, resourceIds);

        Map<String, Resource> hsAllResources = hashResourcesByReference(allResources);

        //transform our resources
        List<AbstractModel> models = new ArrayList<>();
        for (Resource resource: filteredResources) {
            transformResource(resource, models, hsAllResources);
        }

        //set the service provider on all of them
        BigInteger serviceProviderId = AbstractTransformer.transformId(serviceId.toString());
        for (AbstractModel model: models) {
            model.setServiceProviderId(serviceProviderId);
        }

        //hash the models by their type
        Map<Class, List<AbstractModel>> hm = hashModels(models);

        //write each list of models out to an in-memory zip file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (List<AbstractModel> list: hm.values()) {
            AbstractModel first = list.get(0);
            String fileName = first.getClass().getSimpleName() + ".csv";
            byte[] csvBytes = writeToCsv(list);

            zos.putNextEntry(new ZipEntry(fileName));
            zos.write(csvBytes);
        }
        zos.flush();
        zos.close();

        //return as base64 encoded string
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static Map<String, Resource> hashResourcesByReference(List<Resource> allResources) throws Exception {
        Map<String, Resource> ret = new HashMap<>();

        for (Resource resource: allResources) {

            Reference reference = ReferenceHelper.createReference(resource);
            String referenceStr = reference.getReference();
            ret.put(referenceStr, resource);
        }

        return ret;
    }

    private static byte[] writeToCsv(List<AbstractModel> models) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        BufferedWriter bw = new BufferedWriter(osw);
        CSVPrinter csvPrinter = null;
        try {
            csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);

            AbstractModel first = models.get(0);
            first.writeHeaderToCsv(csvPrinter);
            csvPrinter.println();

            for (AbstractModel model: models) {
                model.writeRecordToCsv(csvPrinter);
                csvPrinter.println();
            }

        } finally {
            if (csvPrinter != null) {
                csvPrinter.close();
            }
            /*bw.flush();
            bw.close();*/
        }

        return baos.toByteArray();
    }

    private static Map<Class, List<AbstractModel>> hashModels(List<AbstractModel> models) {
        Map<Class, List<AbstractModel>> hm = new HashMap<>();
        for (AbstractModel model: models) {
            Class cls = model.getClass();
            List<AbstractModel> l = hm.get(cls);
            if (l == null) {
                l = new ArrayList<>();
                hm.put(cls, l);
            }
            l.add(model);
        }
        return hm;
    }

    private static void transformResource(Resource resource, List<AbstractModel> models, Map<String, Resource> hsAllResources) throws Exception {
        if (resource instanceof Patient) {
            //no transformer for this, as we handle these resources when doing EpisodeOfCare resources

        } else if (resource instanceof Condition) {
            ConditionTransformer.transform((Condition)resource, models, hsAllResources);

        } else if (resource instanceof Procedure) {
            ProcedureTransformer.transform((Procedure)resource, models, hsAllResources);

        } else if (resource instanceof ReferralRequest) {
            ReferralRequestTransformer.transform((ReferralRequest)resource, models, hsAllResources);

        } else if (resource instanceof ProcedureRequest) {
            ProcedureRequestTransformer.transform((ProcedureRequest)resource, models, hsAllResources);

        } else if (resource instanceof Schedule) {
            //no transformer for this, as we handle these resources when doing Appointment resources

        } else if (resource instanceof Slot) {
            //no transformer for this, as we handle these resources when doing Appointment resources

        } else if (resource instanceof Practitioner) {
            PractitionerTransformer.transform((Practitioner)resource, models);

        } else if (resource instanceof Observation) {
            ObservationTransformer.transform((Observation)resource, models, hsAllResources);

        } else if (resource instanceof Organization) {
            OrganisationTransformer.transform((Organization)resource, models);

        } else if (resource instanceof MedicationStatement) {
            //nowhere in CEG format for medication statement data

        } else if (resource instanceof MedicationOrder) {
            MedicationOrderTransformer.transform((MedicationOrder)resource, models, hsAllResources);

        } else if (resource instanceof Location) {
            //no transformer for this, as we handle these resources when doing other resources

        } else if (resource instanceof Immunization) {
            ImmunisationTransformer.transform((Immunization)resource, models, hsAllResources);

        } else if (resource instanceof FamilyMemberHistory) {
            FamilyMemberHistoryTransformer.transform((FamilyMemberHistory)resource, models, hsAllResources);

        } else if (resource instanceof EpisodeOfCare) {
            EpisodeOfCareTransformer.transform((EpisodeOfCare)resource, models, hsAllResources);

        } else if (resource instanceof Encounter) {
            //no transformer for this, as we handle these resources when doing other resources

        } else if (resource instanceof Appointment) {
            AppointmentTransformer.transform((Appointment)resource, models, hsAllResources);

        } else if (resource instanceof AllergyIntolerance) {
            AllergyIntoleranceTransformer.transform((AllergyIntolerance)resource, models, hsAllResources);

        } else {
            throw new TransformException("Unsupported FHIR resource type " + resource.getResourceType());
        }
    }

    private static List<Resource> retrieveAllResources(UUID batchId) throws Exception {

        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        LOG.info("Got {} resources for batch {}", resourcesByExchangeBatch.size(), batchId);

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
    }

    private static List<Resource> filterResources(List<Resource> allResources, List<UUID> resourceIds) throws Exception {

        //if we have a list of IDs to restrict on, use it
        Set<UUID> hsResourcesToKeep = null;
        if (resourceIds != null) {
            hsResourcesToKeep = new HashSet<>(resourceIds);
        }

        List<Resource> ret = new ArrayList<>();

        for (Resource resource: allResources) {
            UUID resourceId = UUID.fromString(resource.getId());

            if (hsResourcesToKeep == null
                || hsResourcesToKeep.contains(resourceId)) {
                ret.add(resource);
            }
        }

        return ret;
    }
}
