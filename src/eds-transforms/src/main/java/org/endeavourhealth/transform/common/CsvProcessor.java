package org.endeavourhealth.transform.common;

import org.hl7.fhir.instance.model.*;

import java.util.*;

public class CsvProcessor {

    private static Set<Class> patientResourceClasses = null;
    private static int batchSaveSize = 10000;

    private UUID serviceId = null;
    private UUID systemInstanceId = null;
    private Map<Resource, UUID> resourcesToSave = new HashMap<>();
    private Map<Resource, UUID> resourcesToDelete = new HashMap<>();
    private Map<String, UUID> patientBatchIdMap = new HashMap<>();
    private UUID adminBatchId = null;

    public CsvProcessor(UUID serviceId, UUID systemInstanceId) {
        this.serviceId = serviceId;
        this.systemInstanceId = systemInstanceId;
    }



    public void saveAdminResource(Resource resource) {
        addResourceToQueue(resource, false, getAdminBatchId(), resourcesToSave, serviceId, systemInstanceId);
    }
    public void deleteAdminResource(Resource resource) {
        addResourceToQueue(resource, false, getAdminBatchId(), resourcesToDelete, serviceId, systemInstanceId);
    }

    public void savePatientResource(Resource resource, String sourcePatientId) {
        addResourceToQueue(resource, true, getPatientBatchId(sourcePatientId), resourcesToSave, serviceId, systemInstanceId);
    }
    public void deletePatientResource(Resource resource, String sourcePatientId) {
        addResourceToQueue(resource, true, getPatientBatchId(sourcePatientId), resourcesToDelete, serviceId, systemInstanceId);
    }

    private UUID getAdminBatchId() {
        if (adminBatchId == null) {
            adminBatchId = UUID.randomUUID();
        }
        return adminBatchId;
    }
    private UUID getPatientBatchId(String sourcePatientId) {
        UUID patientBatchId = patientBatchIdMap.get(sourcePatientId);
        if (patientBatchId == null) {
            patientBatchId = UUID.randomUUID();
            patientBatchIdMap.put(sourcePatientId, patientBatchId);
        }
        return patientBatchId;
    }


    private static void addResourceToQueue(Resource resource,
                                           boolean expectingPatientResource,
                                           UUID batchId,
                                           Map<Resource, UUID> map,
                                           UUID serviceId,
                                           UUID systemInstanceId) {

        if (isPatientResource(resource) != expectingPatientResource) {
            throw new RuntimeException("Trying to treat patient resource as admin or vice versa");
        }

        map.put(resource, batchId);

        if (map.size() >= batchSaveSize) {
            savePendingResources(map, serviceId, systemInstanceId);
        }
    }
    private static void savePendingResources(Map<Resource, UUID> map, UUID serviceId, UUID systemInstanceId) {

        Map<Resource, UUID> saveMap = new HashMap<>(map);
        map.clear();

        List<Resource> resources = new ArrayList<>(saveMap.keySet());
        IdHelper.mapIds(serviceId, systemInstanceId, resources);

        //TODO - invoke filer for resources
    }

    public void processingCompleted() {
        savePendingResources(resourcesToSave, serviceId, systemInstanceId);
        savePendingResources(resourcesToDelete, serviceId, systemInstanceId);

        //TODO - send transaction IDs to next queue to start outgoing pipeline
    }

    private static boolean isPatientResource(Resource resource) {
        Class cls = resource.getClass();

        if (patientResourceClasses == null) {
            Set<Class> set = new HashSet<>();
            set.add(AllergyIntolerance.class);
            set.add(Appointment.class);
            set.add(Condition.class);
            set.add(DiagnosticOrder.class);
            set.add(DiagnosticReport.class);
            set.add(Encounter.class);
            set.add(EpisodeOfCare.class);
            set.add(FamilyMemberHistory.class);
            set.add(Immunization.class);
            set.add(MedicationOrder.class);
            set.add(MedicationStatement.class);
            set.add(Observation.class);
            set.add(Order.class);
            set.add(Patient.class);
            set.add(Procedure.class);
            set.add(ProcedureRequest.class);
            set.add(ReferralRequest.class);
            set.add(RelatedPerson.class);
            set.add(Schedule.class);
            set.add(Specimen.class);

            patientResourceClasses = set;
        }

        return patientResourceClasses.contains(cls);
    }

    /**
     * just to allow us to potentually tune batch size via config or in real-time
     */
    public static void setBatchSaveSize(int batchSaveSize) {
        CsvProcessor.batchSaveSize = batchSaveSize;
    }
}
