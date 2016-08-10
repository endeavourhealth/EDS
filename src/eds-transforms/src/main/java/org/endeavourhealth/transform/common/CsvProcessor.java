package org.endeavourhealth.transform.common;

import com.datastax.driver.core.utils.UUIDs;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CsvProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CsvProcessor.class);

    private static final int THREAD_POOL_SIZE = 10; //arbitrary choice

    private static Set<Class> patientResourceClasses = null;

    private final UUID exchangeId;
    private final UUID serviceId;
    private final UUID systemId;
    private final FhirStorageService storageService;
    private final ExchangeBatchRepository exchangeBatchRepository;

    private ReentrantLock lock = new ReentrantLock();
    private Map<String, UUID> patientBatchIdMap = new ConcurrentHashMap<>();
    private UUID adminBatchId = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private AtomicInteger threadPoolQueueSize = new AtomicInteger();
    private Map<UUID, AtomicInteger> countResourcesSaved = new ConcurrentHashMap<>();
    private Map<UUID, AtomicInteger> countResourcesDeleted = new ConcurrentHashMap<>();
    private Map<Future, ?> futures = new ConcurrentHashMap<>();

    public CsvProcessor(UUID exchangeId, UUID serviceId, UUID systemId) {
        this.exchangeId = exchangeId;
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.storageService = new FhirStorageService(serviceId, systemId);
        this.exchangeBatchRepository = new ExchangeBatchRepository();
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

        //validate we're treating the resoure properly as admin / patient
        if (isPatientResource(resource) != expectingPatientResource) {
            throw new PatientResourceException(resource.getResourceType(), expectingPatientResource);
        }

        //increment our counters for auditing
        if (toDelete) {
            countResourcesDeleted.get(batchId).incrementAndGet();
        } else {
            countResourcesSaved.get(batchId).incrementAndGet();
        }

        threadPoolQueueSize.incrementAndGet();
        Future future = threadPool.submit(new WorkerCallable(resource, batchId, toDelete, mapIds));
        //futures.put(future, null);
    }

    private void checkFutures() {

/*        Lists.n

        //check and remoev
        Collections.newSetFromMap()
        futures.keySet();*/
    }


    private UUID getAdminBatchId() {
        if (adminBatchId == null) {

            try {
                lock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to lock
                if (adminBatchId == null) {
                    adminBatchId = UUIDs.timeBased();
                    saveExchangeBatch(adminBatchId);

                    countResourcesDeleted.put(adminBatchId, new AtomicInteger());
                    countResourcesSaved.put(adminBatchId, new AtomicInteger());
                }
            } finally {
                lock.unlock();
            }
        }
        return adminBatchId;
    }
    private UUID getPatientBatchId(String patientId) {
        UUID patientBatchId = patientBatchIdMap.get(patientId);
        if (patientBatchId == null) {

            try {
                lock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to lock
                patientBatchId = patientBatchIdMap.get(patientId);
                if (patientBatchId == null) {
                    patientBatchId = UUIDs.timeBased();
                    patientBatchIdMap.put(patientId, patientBatchId);
                    saveExchangeBatch(patientBatchId);

                    countResourcesDeleted.put(patientBatchId, new AtomicInteger());
                    countResourcesSaved.put(patientBatchId, new AtomicInteger());
                }
            } finally {
                lock.unlock();
            }
        }
        return patientBatchId;
    }
    private void saveExchangeBatch(UUID batchId) {
        ExchangeBatch exchangeBatch = new ExchangeBatch();
        exchangeBatch.setBatchId(batchId);
        exchangeBatch.setExchangeId(exchangeId);
        exchangeBatch.setInsertedAt(new Date());
        exchangeBatchRepository.save(exchangeBatch);
    }

    /**
     * called after all content has been processed. It blocks until all operations have
     * been completed in the thread pool, then returns the distinct batch IDs created
     */
    public List<UUID> getBatchIdsCreated() throws Exception {

        //shutdown the threadpool and wait for all runnables to complete
        threadPool.shutdown();
        while (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
            LOG.trace("Waiting for thread pool to complete {} tasks", threadPoolQueueSize.get());
        }

        //check any remaining futures, to see if any exceptions were raised
        checkFutures();

        logResults();

        return getAllBatchIds();
    }

    private void logResults() {

        int totalSaved = 0;
        int totalDeleted = 0;

        LOG.info("CSV processing completed");

        int saved = countResourcesSaved.get(adminBatchId).get();
        int deleted = countResourcesDeleted.get(adminBatchId).get();
        LOG.info("Saved {} and deleted {} admin resources", saved, deleted);
        totalSaved += saved;
        totalDeleted += deleted;

        Iterator<String> it = patientBatchIdMap.keySet().iterator();
        while (it.hasNext()) {
            String patientId = it.next();
            UUID batchId = patientBatchIdMap.get(patientId);

            saved = countResourcesSaved.get(batchId).get();
            deleted = countResourcesDeleted.get(batchId).get();
            LOG.info("Saved {} and deleted {} admin resources for patient {}", saved, deleted, patientId);
            totalSaved += saved;
            totalDeleted += deleted;
        }

        LOG.info("CSV processing completed, saving {} resources, deleting {} for {} distinct patients", totalSaved, totalDeleted, patientBatchIdMap.size());
    }

    private List<UUID> getAllBatchIds() {
        List<UUID> batchIds = new ArrayList<>();
        if (adminBatchId != null) {
            batchIds.add(adminBatchId);
        }
        Iterator<UUID> it = patientBatchIdMap.values().iterator();
        while (it.hasNext()) {
            UUID batchId = it.next();
            batchIds.add(batchId);
        }
        return batchIds;
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

    public UUID getSystemId() {
        return systemId;
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
                IdHelper.mapIds(serviceId, systemId, resource);
            }

            List<Resource> list = new ArrayList<>();
            list.add(resource);

            if (isDelete) {
                storageService.exchangeBatchDelete(exchangeId, batchUuid, list);
            } else {
                storageService.exchangeBatchUpdate(exchangeId, batchUuid, list);
            }

            threadPoolQueueSize.decrementAndGet();
            return null;
        }
    }
}
