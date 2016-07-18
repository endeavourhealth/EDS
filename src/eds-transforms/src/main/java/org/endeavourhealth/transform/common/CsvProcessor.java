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
    private Set<Resource> resourcesToIdMap = new HashSet<>();
    private Map<String, UUID> patientBatchIdMap = new HashMap<>();
    private UUID adminBatchId = null;

    public CsvProcessor(UUID serviceId, UUID systemInstanceId) {
        this.serviceId = serviceId;
        this.systemInstanceId = systemInstanceId;
    }



    public void saveAdminResource(Resource resource) {
        saveAdminResource(resource, true);
    }
    public void saveAdminResource(Resource resource, boolean mapIds) {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), resourcesToSave);
    }

    public void deleteAdminResource(Resource resource) {
        deleteAdminResource(resource, true);
    }
    public void deleteAdminResource(Resource resource, boolean mapIds) {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), resourcesToDelete);
    }

    public void savePatientResource(Resource resource, String sourcePatientId) {
        savePatientResource(resource, true, sourcePatientId);
    }
    public void savePatientResource(Resource resource, boolean mapIds, String sourcePatientId) {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(sourcePatientId), resourcesToSave);
    }

    public void deletePatientResource(Resource resource, String sourcePatientId) {
        deletePatientResource(resource, true, sourcePatientId);
    }
    public void deletePatientResource(Resource resource, boolean mapIds, String sourcePatientId) {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(sourcePatientId), resourcesToDelete);
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


    private void addResourceToQueue(Resource resource,
                                   boolean expectingPatientResource,
                                   boolean mapIds,
                                   UUID batchId,
                                   Map<Resource, UUID> map) {

        if (isPatientResource(resource) != expectingPatientResource) {
            throw new RuntimeException("Trying to treat patient resource as admin or vice versa");
        }

        map.put(resource, batchId);

        if (mapIds) {
            resourcesToIdMap.add(resource);
        }

        if (map.size() >= batchSaveSize) {
            savePendingResources(map);
        }
    }
    private void savePendingResources(Map<Resource, UUID> map) {

        if (!resourcesToIdMap.isEmpty()) {
            List<Resource> resources = new ArrayList<>(resourcesToIdMap);
            resourcesToIdMap.clear();

            IdHelper.mapIds(serviceId, systemInstanceId, resources);
        }


        Map<Resource, UUID> saveMap = new HashMap<>(map);
        map.clear();
        //TODO - invoke filer for resources
    }

    public void processingCompleted() {
        savePendingResources(resourcesToSave);
        savePendingResources(resourcesToDelete);

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

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getSystemInstanceId() {
        return systemInstanceId;
    }
}
