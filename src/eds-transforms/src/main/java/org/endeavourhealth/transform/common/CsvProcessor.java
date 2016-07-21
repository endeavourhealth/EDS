package org.endeavourhealth.transform.common;

import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CsvProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CsvProcessor.class);

    /*private static final int MAPPING_BATCH_SIZE = 5000;
    private static final int SAVING_BATCH_SIZE = 10;*/
    private static final int THREAD_POOL_SIZE = 10;

    private static Set<Class> patientResourceClasses = null;

    private Exchange exchange = null;
    private UUID serviceId = null;
    private UUID systemInstanceId = null;
    private FhirStorageService storageService = null;

    private Map<String, UUID> patientBatchIdMap = new HashMap<>();
    private UUID adminBatchId = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
/*    private List<ResourceWrapper> resourcesToMap = new ArrayList<>();
    private List<ResourceWrapper> resourcesToSave = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    private Stack<Future<?>> mappingFutures = new Stack<>();*/
    private ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();

    public CsvProcessor(Exchange exchange, UUID serviceId, UUID systemInstanceId) {
        this.exchange = exchange;
        this.serviceId = serviceId;
        this.systemInstanceId = systemInstanceId;
        this.storageService = new FhirStorageService(serviceId, systemInstanceId);
    }


    public void saveAdminResource(Resource resource) throws Exception {
        saveAdminResource(resource, true);
    }
    public void saveAdminResource(Resource resource, boolean mapIds) throws Exception {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), false);
    }

    public void deleteAdminResource(Resource resource) throws Exception {
        deleteAdminResource(resource, true);
    }
    public void deleteAdminResource(Resource resource, boolean mapIds) throws Exception {
        addResourceToQueue(resource, false, mapIds, getAdminBatchId(), true);
    }

    public void savePatientResource(Resource resource, String patientId) throws Exception {
        savePatientResource(resource, true, patientId);
    }
    public void savePatientResource(Resource resource, boolean mapIds, String patientId) throws Exception {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(patientId), false);
    }

    public void deletePatientResource(Resource resource, String patientId) throws Exception {
        deletePatientResource(resource, true, patientId);
    }
    public void deletePatientResource(Resource resource, boolean mapIds, String patientId) throws Exception {
        addResourceToQueue(resource, true, mapIds, getPatientBatchId(patientId), true);
    }

    private void addResourceToQueue(Resource resource,
                                    boolean expectingPatientResource,
                                    boolean mapIds,
                                    UUID batchId,
                                    boolean toDelete) throws Exception {

        if (isPatientResource(resource) != expectingPatientResource) {
            throw new PatientResourceException(resource.getResourceType(), expectingPatientResource);
        }

        threadPool.submit(new WorkerCallable(resource, batchId, toDelete, mapIds));
    }

    /*private void addResourceToQueue(Resource resource,
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
    }*/


    private UUID getAdminBatchId() {
        if (adminBatchId == null) {
            adminBatchId = UUID.randomUUID();
            saveExchangeBatch(adminBatchId);
        }
        return adminBatchId;
    }
    private UUID getPatientBatchId(String patientId) {
        UUID patientBatchId = patientBatchIdMap.get(patientId);
        if (patientBatchId == null) {
            patientBatchId = UUID.randomUUID();
            patientBatchIdMap.put(patientId, patientBatchId);
            saveExchangeBatch(patientBatchId);
        }
        return patientBatchId;
    }
    private void saveExchangeBatch(UUID batchId) {
        ExchangeBatch exchangeBatch = new ExchangeBatch();
        exchangeBatch.setBatchId(batchId);
        exchangeBatch.setExchangeId(exchange.getExchangeId());
        exchangeBatch.setInsertedAt(new Date());
        exchangeBatchRepository.save(exchangeBatch);
    }


    public void processingCompleted() {

        LOG.trace("Processing completion starting");

        //shutdown the threadpool and wait for all runnables to complete
        threadPool.shutdown();
        try {
            LOG.trace("Waiting for thread pool to complete");
            threadPool.awaitTermination(24, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            LOG.error("Error waiting for pool to finish", ex);
        }

        //update the Exchange with the batch IDs, for the next step in the pipeline
        List<String> batchIds = new ArrayList<>();
        if (adminBatchId != null) {
            batchIds.add(adminBatchId.toString());
        }
        Iterator<UUID> it = patientBatchIdMap.values().iterator();
        while (it.hasNext()) {
            UUID batchId = it.next();
            batchIds.add(batchId.toString());
        }

        exchange.setHeader(HeaderKeys.BatchIds, String.join(";", batchIds));

        LOG.trace("Processing fully completed");
    }

    /*public void processingCompleted() {

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

        //TODO - send batch IDs to next queue to start outgoing pipeline

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
    }*/

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

    class WorkerCallable implements Callable {

        private Resource resource = null;
        private UUID batchUuid = null;
        private boolean isDelete = false;
        private boolean mapIds = false;

        public WorkerCallable(Resource resource, UUID batchUuid, boolean isDelete, boolean mapIds) {
            this.resource = resource;
            this.batchUuid = batchUuid;
            this.isDelete = isDelete;
            this.mapIds = mapIds;
        }

        @Override
        public Object call() throws Exception {

            if (mapIds) {
                IdHelper.mapIds(serviceId, systemInstanceId, resource);
            }

            List<Resource> list = new ArrayList<>();
            list.add(resource);

            if (isDelete) {
                storageService.exchangeBatchDelete(exchange.getExchangeId(), batchUuid, list);
            } else {
                storageService.exchangeBatchUpdate(exchange.getExchangeId(), batchUuid, list);
            }

            return null;
        }
    }

    /*class SaveCallable implements Callable {
        private List<ResourceWrapper> resourceWrappers = null;

        public SaveCallable(List<ResourceWrapper> resourceWrappers) {
            this.resourceWrappers = resourceWrappers;
        }

        @Override
        public Object call() throws Exception {

            LOG.trace("Saving " + resourceWrappers.size() + " resources");
            Map<UUID, List<Resource>> hmToSave = new HashMap<>();
            Map<UUID, List<Resource>> hmToDelete = new HashMap<>();

            for (ResourceWrapper resourceWrapper: resourceWrappers) {

                Map<UUID, List<Resource>> map = null;
                if (resourceWrapper.isDelete()) {
                    map = hmToDelete;
                } else {
                    map = hmToSave;
                }

                List<Resource> list = map.get(resourceWrapper.getBatchUuid());
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(resourceWrapper.getBatchUuid(), list);
                }
                list.add(resourceWrapper.getResource());
            }

            Iterator<UUID> iterator = hmToSave.keySet().iterator();
            while (iterator.hasNext()) {
                UUID batchId = iterator.next();
                List<Resource> list = hmToSave.get(batchId);
                storageService.exchangeBatchUpdate(exchangeId, batchId, list);
            }

            iterator = hmToDelete.keySet().iterator();
            while (iterator.hasNext()) {
                UUID batchId = iterator.next();
                List<Resource> list = hmToDelete.get(batchId);
                storageService.exchangeBatchDelete(exchangeId, batchId, list);
            }

            LOG.trace("Finished " + resourceWrappers.size() + " resources");
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

            LOG.trace("Finished mapping IDs for " + resourceWrappers.size() + " resources");
            return null;
        }

        private void mapId(ResourceWrapper resourceWrapper) {
            //LOG.trace("Doing " + resourceWrapper.getResource());
            IdHelper.mapIds(serviceId, systemInstanceId, resourceWrapper.getResource());
            //LOG.trace("Done " + resourceWrapper.getResource());
            addResourceWrapperToQueue(resourceWrapper, false);
            //LOG.trace("Added to queue " + resourceWrapper.getResource());
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
    }*/
}
