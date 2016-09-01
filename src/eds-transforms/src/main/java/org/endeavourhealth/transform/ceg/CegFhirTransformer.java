package org.endeavourhealth.transform.ceg;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.transforms.PatientTransformer;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CegFhirTransformer {

    public static String transformFromFhir(UUID batchId, List<UUID> resourceIds) throws Exception {

        List<AbstractModel> models = new ArrayList<>();

        List<Resource> resources = retrieveResources(batchId, resourceIds);
        for (Resource resource: resources) {
            transformResource(resource, models);
        }

        //hash the models by their type
        Map<Class, List<AbstractModel>> hm = hashModels(models);

        //write each list of models out to an in-memory zip file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (List<AbstractModel> list: hm.values()) {
            AbstractModel first = list.get(0);
            String fileName = first.getCsvFileName();
            byte[] csvBytes = writeToCsv(list);

            zos.putNextEntry(new ZipEntry(fileName));
            zos.write(csvBytes);
        }

        zos.close();

        //return as base64 encoded string
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] writeToCsv(List<AbstractModel> models){
        return null;
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

    private static void transformResource(Resource resource, List<AbstractModel> models) throws Exception {
        if (resource instanceof Patient) {
            PatientTransformer.transformPatient((Patient)resource, models);
        } else if (resource instanceof Condition) {

        } else if (resource instanceof Procedure) {

        } else if (resource instanceof ReferralRequest) {

        } else if (resource instanceof ProcedureRequest) {

        } else if (resource instanceof Schedule) {

        } else if (resource instanceof Slot) {

        } else if (resource instanceof Practitioner) {

        } else if (resource instanceof Observation) {

        } else if (resource instanceof Organization) {

        } else if (resource instanceof MedicationStatement) {

        } else if (resource instanceof MedicationOrder) {

        } else if (resource instanceof Location) {

        } else if (resource instanceof Immunization) {

        } else if (resource instanceof FamilyMemberHistory) {

        } else if (resource instanceof EpisodeOfCare) {

        } else if (resource instanceof Encounter) {

        } else if (resource instanceof Appointment) {

        } else if (resource instanceof AllergyIntolerance) {

        } else {
            throw new TransformException("Unsupported FHIR resource type " + resource.getResourceType());
        }
    }

    private static List<Resource> retrieveResources(UUID batchId, List<UUID> resourceIds) throws Exception {

        //retrieve the resources and strip out any that aren't in the list of resource IDs
        List<ResourceByExchangeBatch> resourcesByExchangeBatch = new ResourceRepository().getResourcesForBatch(batchId);
        Set<UUID> hsResourcesToKeep = new HashSet<>(resourceIds);

        List<Resource> ret = new ArrayList<>();

        for (ResourceByExchangeBatch resourceByExchangeBatch: resourcesByExchangeBatch) {
            UUID resourceId = resourceByExchangeBatch.getResourceId();
            if (hsResourcesToKeep.contains(resourceId)) {

                String json = resourceByExchangeBatch.getResourceData();
                Resource r = new JsonParser().parse(json);
                ret.add(r);
            }
        }

        return ret;
    }
}
