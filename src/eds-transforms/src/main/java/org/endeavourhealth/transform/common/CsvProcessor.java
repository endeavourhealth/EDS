package org.endeavourhealth.transform.common;

import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CsvProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CsvProcessor.class);

    private static final int MAPPING_BATCH_SIZE = 5000;
    private static final int SAVING_BATCH_SIZE = 10000;
    private static final int THREAD_POOL_SIZE = 5;

    private static Set<Class> patientResourceClasses = null;

    private UUID serviceId = null;
    private UUID systemInstanceId = null;
    private Map<UUID, UUID> patientBatchIdMap = new HashMap<>();
    private UUID adminBatchId = null;

    private ExecutorService threadPool = null;
    private List<ResourceWrapper> resourcesToMap = new ArrayList<>();
    private List<ResourceWrapper> resourcesToSave = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    private Stack<Future<?>> mappingFutures = new Stack<>();

    public CsvProcessor(UUID serviceId, UUID systemInstanceId) {
        this.serviceId = serviceId;
        this.systemInstanceId = systemInstanceId;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }



    public void saveAdminResource(Resource resource) {
        saveAdminResource(resource, true);
    }
    public void saveAdminResource(Resource resource, boolean mapIds) {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), false);
    }

    public void deleteAdminResource(Resource resource) {
        deleteAdminResource(resource, true);
    }
    public void deleteAdminResource(Resource resource, boolean mapIds) {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), true);
    }

    public void savePatientResource(Resource resource, UUID patientUuid) {
        savePatientResource(resource, true, patientUuid);
    }
    public void savePatientResource(Resource resource, boolean mapIds, UUID patientUuid) {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(patientUuid), false);
    }

    public void deletePatientResource(Resource resource, UUID patientUuid) {
        deletePatientResource(resource, true, patientUuid);
    }
    public void deletePatientResource(Resource resource, boolean mapIds, UUID patientUuid) {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(patientUuid), true);
    }

    private void addResourceToQueue(Resource resource,
                                    boolean expectingPatientResource,
                                    boolean mapIds,
                                    UUID batchId,
                                    boolean toDelete) {

        if (isPatientResource(resource) != expectingPatientResource) {
            throw new RuntimeException("Trying to treat patient resource as admin or vice versa");
        }

        addResourceWrapperToQueue(new ResourceWrapper(resource, batchId, toDelete), mapIds);
    }
    private void addResourceWrapperToQueue(ResourceWrapper resourceWrapper, boolean mapIds) {

        //make sure to get the lock before manipulating any of the queues
        lock.lock();

        if (mapIds) {
            resourcesToMap.add(resourceWrapper);

            //if we've got a batch to map, then set that off
            if (resourcesToMap.size() >= MAPPING_BATCH_SIZE) {
                mapIds();
            }
        } else {
            resourcesToSave.add(resourceWrapper);

            //if we've got a batch to map, then set that off
            if (resourcesToSave.size() >= SAVING_BATCH_SIZE) {
                saveResources();
            }
        }

        lock.unlock();
    }

    private void mapIds() {
        Future<?> future = threadPool.submit(new MapIpsCallable(new ArrayList<>(resourcesToMap)));
        mappingFutures.add(future);

        resourcesToMap.clear();
    }
    private void saveResources() {
        threadPool.submit(new SaveCallable(new ArrayList<>(resourcesToMap)));
        resourcesToSave.clear();
    }


    private UUID getAdminBatchId() {
        if (adminBatchId == null) {
            adminBatchId = UUID.randomUUID();
        }
        return adminBatchId;
    }
    private UUID getPatientBatchId(UUID patientUuid) {
        UUID patientBatchId = patientBatchIdMap.get(patientUuid);
        if (patientBatchId == null) {
            patientBatchId = UUID.randomUUID();
            patientBatchIdMap.put(patientUuid, patientBatchId);
        }
        return patientBatchId;
    }

    public void processingCompleted() {

        LOG.trace("Processing completion starting");

        //start off any mapping and saving we can
        lock.lock();
        mapIds();
        saveResources();
        lock.unlock();

        //then we need to wait until all mapping processes have been finished
        while (stillMapping()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOG.error("Thread interrupted", ex);
            }
        }

        //once all mapping operations are completed, we can start off the last saving process and shutdown the pool
        lock.lock();
        saveResources();
        lock.unlock();

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            LOG.error("Error waiting for pool to finish", ex);
        }

        //TODO - send transaction IDs to next queue to start outgoing pipeline

        LOG.trace("Processing fully completed");
    }

    private boolean stillMapping() {
        while (!mappingFutures.isEmpty()) {

            if (mappingFutures.peek().isDone()) {
                mappingFutures.pop();
            } else {
                return true;
            }
        }
        return false;
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

            //although Slot isn't technically linked to a patient, it is saved at the same time as
            //Appointment resources, so should be treated as one
            set.add(Slot.class);

            patientResourceClasses = set;
        }

        return patientResourceClasses.contains(cls);
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getSystemInstanceId() {
        return systemInstanceId;
    }

    class SaveCallable implements Callable {
        private List<ResourceWrapper> resourceWrappers = null;

        public SaveCallable(List<ResourceWrapper> resourceWrappers) {
            this.resourceWrappers = resourceWrappers;
        }

        @Override
        public Object call() throws Exception {

            //work out an ordered list of the resource types, using a hashSet for speed of checking
            List<ResourceType> resourceTypes = new ArrayList<>();
            Set<ResourceType> resourceTypeSet = new HashSet<>();
            Map<ResourceType, List<ResourceWrapper>> mapToSave = new HashMap<>();
            Map<ResourceType, List<ResourceWrapper>> mapToDelete = new HashMap<>();

            for (ResourceWrapper resourceWrapper: resourceWrappers) {

                ResourceType resourceType = resourceWrapper.getResource().getResourceType();
                if (!resourceTypeSet.contains(resourceType)) {
                    resourceTypes.add(resourceType);
                    resourceTypeSet.add(resourceType);
                }

                Map<ResourceType, List<ResourceWrapper>> map = null;
                if (resourceWrapper.isDelete()) {
                    map = mapToDelete;
                } else {
                    map = mapToSave;
                }

                List<ResourceWrapper> list = map.get(resourceType);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(resourceType, list);
                }
                list.add(resourceWrapper);
            }

            for (ResourceType resourceType: resourceTypes) {

                List<ResourceWrapper> toSave = mapToSave.get(resourceType);
                if (toSave != null) {

                }
            }

            for (ResourceType resourceType: resourceTypes) {

                List<ResourceWrapper> toDelete = mapToDelete.get(resourceType);
                if (toDelete != null) {

                }
            }


            //TODO - invoke filer to save resources, saving by resource type doing saves before deletes

            return null;
        }
    }

    class MapIpsCallable implements Callable {

        private List<ResourceWrapper> resourceWrappers = null;

        public MapIpsCallable(List<ResourceWrapper> resourceWrappers) {
            this.resourceWrappers = resourceWrappers;
        }

        @Override
        public Object call() throws Exception {

            LOG.trace("Mapping IDs for " + resourceWrappers.size() + " resources");
            resourceWrappers.parallelStream()
                    .forEach((resourceWrapper) -> { mapId(resourceWrapper); });
            return null;
        }

        private void mapId(ResourceWrapper resourceWrapper) {
            IdHelper.mapIds(serviceId, systemInstanceId, resourceWrapper.getResource());
            addResourceWrapperToQueue(resourceWrapper, false);
        }
    }

    class ResourceWrapper {
        private Resource resource = null;
        private UUID batchUuid = null;
        private boolean isDelete = false;

        public ResourceWrapper(Resource resource, UUID batchUuid, boolean isDelete) {
            this.resource = resource;
            this.batchUuid = batchUuid;
            this.isDelete = isDelete;
        }

        public Resource getResource() {
            return resource;
        }

        public UUID getBatchUuid() {
            return batchUuid;
        }

        public boolean isDelete() {
            return isDelete;
        }
    }
}
