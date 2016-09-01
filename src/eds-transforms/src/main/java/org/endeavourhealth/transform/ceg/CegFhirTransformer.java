package org.endeavourhealth.transform.ceg;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.transforms.*;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CegFhirTransformer {

    public static String transformFromFhir(UUID serviceId,
                                           UUID orgNationalId,
                                           UUID batchId,
                                           List<UUID> resourceIds) throws Exception {

        List<AbstractModel> models = new ArrayList<>();

        //transform our resources
        List<Resource> resources = retrieveResources(batchId, resourceIds);
        for (Resource resource: resources) {
            transformResource(resource, models);
        }

        //set the service provider on all of them
        //TODO - set serviceProviderId

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

        zos.close();

        //return as base64 encoded string
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
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
            bw.flush();
            bw.close();
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

    private static void transformResource(Resource resource, List<AbstractModel> models) throws Exception {
        if (resource instanceof Patient) {
            PatientTransformer.transform((Patient)resource, models);

        } else if (resource instanceof Condition) {
            ConditionTransformer.transform((Condition)resource, models);

        } else if (resource instanceof Procedure) {
            ProcedureTransformer.transform((Procedure)resource, models);

        } else if (resource instanceof ReferralRequest) {
            ReferralRequestTransformer.transform((ReferralRequest)resource, models);

        } else if (resource instanceof ProcedureRequest) {
            ProcedureRequestTransformer.transform((ProcedureRequest)resource, models);

        } else if (resource instanceof Schedule) {
            //no transformer for this, as we handle these resources when doing Appointment resources

        } else if (resource instanceof Slot) {
            //no transformer for this, as we handle these resources when doing Appointment resources

        } else if (resource instanceof Practitioner) {
            PractitionerTransformer.transform((Practitioner)resource, models);

        } else if (resource instanceof Observation) {
            ObservationTransformer.transform((Observation)resource, models);

        } else if (resource instanceof Organization) {
            OrganisationTransformer.transform((Organization)resource, models);

        } else if (resource instanceof MedicationStatement) {


        } else if (resource instanceof MedicationOrder) {

        } else if (resource instanceof Location) {
            //no fields in data for any of location data

        } else if (resource instanceof Immunization) {

        } else if (resource instanceof FamilyMemberHistory) {

        } else if (resource instanceof EpisodeOfCare) {

        } else if (resource instanceof Encounter) {

        } else if (resource instanceof Appointment) {
            AppointmentTransformer.transform((Appointment)resource, models);

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
