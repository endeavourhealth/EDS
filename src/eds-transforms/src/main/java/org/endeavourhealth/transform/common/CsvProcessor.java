package org.endeavourhealth.transform.common;

import com.datastax.driver.core.utils.UUIDs;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.CallableError;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.endeavourhealth.transform.emis.csv.ThreadPool;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CsvProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CsvProcessor.class);

    private static Set<Class> patientResourceClasses = null;

    private final UUID exchangeId;
    private final UUID serviceId;
    private final UUID systemId;
    private final FhirStorageService storageService;
    private final ExchangeBatchRepository exchangeBatchRepository;
    private final TransformError transformError;
    //private final ExchangeTransformAudit transformAudit;
    //private final Map<String, String> resourceTypes; //although a set would be idea, a map allows safe multi-thread access

    //batch IDs
    private ReentrantLock batchIdLock = new ReentrantLock();
    private Map<String, UUID> patientBatchIdMap = new ConcurrentHashMap<>();
    private UUID adminBatchId = null;

    //threading
    private ThreadPool threadPool = null;

    //counts
    private Map<UUID, AtomicInteger> countResourcesSaved = new ConcurrentHashMap<>();
    private Map<UUID, AtomicInteger> countResourcesDeleted = new ConcurrentHashMap<>();


    public CsvProcessor(UUID exchangeId, UUID serviceId, UUID systemId, TransformError transformError, int maxFilingThreads) {
        this.exchangeId = exchangeId;
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.storageService = new FhirStorageService(serviceId, systemId);
        this.exchangeBatchRepository = new ExchangeBatchRepository();
        this.transformError = transformError;
        this.threadPool = new ThreadPool(maxFilingThreads, 50000);
    }


    public void saveAdminResource(CsvCurrentState parserState, Resource... resources) throws Exception {
        saveAdminResource(parserState, true, resources);
    }
    public void saveAdminResource(CsvCurrentState parserState, boolean mapIds, Resource... resources) throws Exception {
        addResourceToQueue(parserState, false, mapIds, getAdminBatchId(), false, resources);
    }

    public void deleteAdminResource(CsvCurrentState parserState, Resource... resources) throws Exception {
        deleteAdminResource(parserState, true, resources);
    }
    public void deleteAdminResource(CsvCurrentState parserState, boolean mapIds, Resource... resources) throws Exception {
        addResourceToQueue(parserState, false, mapIds, getAdminBatchId(), true, resources);
    }

    public void savePatientResource(CsvCurrentState parserState, String patientId, Resource... resources) throws Exception {
        savePatientResource(parserState, true, patientId, resources);
    }
    public void savePatientResource(CsvCurrentState parserState, boolean mapIds, String patientId, Resource... resources) throws Exception {
        addResourceToQueue(parserState, true, mapIds, getPatientBatchId(patientId), false, resources);
    }

    public void deletePatientResource(CsvCurrentState parserState, String patientId, Resource... resources) throws Exception {
        deletePatientResource(parserState, true, patientId, resources);
    }
    public void deletePatientResource(CsvCurrentState parserState, boolean mapIds, String patientId, Resource... resources) throws Exception {
        addResourceToQueue(parserState, true, mapIds, getPatientBatchId(patientId), true, resources);
    }

    private void addResourceToQueue(CsvCurrentState parserState,
                                    boolean expectingPatientResource,
                                    boolean mapIds,
                                    UUID batchId,
                                    boolean toDelete,
                                    Resource... resources) throws Exception {

        for (Resource resource: resources) {
            //validate we're treating the resource properly as admin / patient
            if (isPatientResource(resource) != expectingPatientResource) {
                throw new PatientResourceException(resource.getResourceType(), expectingPatientResource);
            }

            /*String resourceType = resource.getResourceType().toString();
            resourceTypes.put(resourceType, resourceType);*/

            //increment our counters for auditing
            if (toDelete) {
                countResourcesDeleted.get(batchId).incrementAndGet();
            } else {
                countResourcesSaved.get(batchId).incrementAndGet();
            }
        }

        List<CallableError> errors = threadPool.submit(new MapAndSaveResourceTask(parserState, batchId, toDelete, mapIds, resources));
        handleErrors(errors);
    }

    private UUID getAdminBatchId() {
        if (adminBatchId == null) {

            try {
                batchIdLock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to batchIdLock
                if (adminBatchId == null) {
                    adminBatchId = UUIDs.timeBased();
                    saveExchangeBatch(adminBatchId);

                    countResourcesDeleted.put(adminBatchId, new AtomicInteger());
                    countResourcesSaved.put(adminBatchId, new AtomicInteger());
                }
            } finally {
                batchIdLock.unlock();
            }
        }
        return adminBatchId;
    }
    private UUID getPatientBatchId(String patientId) {
        UUID patientBatchId = patientBatchIdMap.get(patientId);
        if (patientBatchId == null) {

            try {
                batchIdLock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to batchIdLock
                patientBatchId = patientBatchIdMap.get(patientId);
                if (patientBatchId == null) {
                    patientBatchId = UUIDs.timeBased();
                    patientBatchIdMap.put(patientId, patientBatchId);
                    saveExchangeBatch(patientBatchId);

                    countResourcesDeleted.put(patientBatchId, new AtomicInteger());
                    countResourcesSaved.put(patientBatchId, new AtomicInteger());
                }
            } finally {
                batchIdLock.unlock();
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

        //wait for all tasks to be completed
        List<CallableError> errors = threadPool.waitAndStop();
        handleErrors(errors);

        //update the resource types used
        //saveResourceTypesUsed();

        //log out counts of what we processed
        logResults();

        return getAllBatchIds();
    }

    private void handleErrors(List<CallableError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        for (CallableError error: errors) {

            MapAndSaveResourceTask callable = (MapAndSaveResourceTask)error.getCallable();
            Exception exception = error.getException();
            CsvCurrentState parserState = callable.getParserState();

            //if we had an error that doesn't have a CSV state, then it's not something that can be attributed
            //to a specific row in a CSV file, and so should be treated as a fatal exception
            if (parserState == null) {
                throw exception;
            }

            logTransformRecordError(exception, parserState);
        }
    }

    /*private void saveResourceTypesUsed() {

        ResourceRepository resourceRepository = new ResourceRepository();

        Iterator<String> it = resourceTypes.keySet().iterator();
        while (it.hasNext()) {
            String resourceType = it.next();
            ResourceTypesUsed resourceTypesUsed = new ResourceTypesUsed();
            resourceTypesUsed.setServiceId(serviceId);
            resourceTypesUsed.setSystemId(systemId);
            resourceTypesUsed.setResourceType(resourceType);

            resourceRepository.save(resourceTypesUsed);
        }
    }*/


    private void logResults() {

        int totalSaved = 0;
        int totalDeleted = 0;

        LOG.info("CSV processing completed");

        int saved = 0;
        int deleted = 0;
        if (adminBatchId != null) {
            saved += countResourcesSaved.get(adminBatchId).get();
            deleted += countResourcesDeleted.get(adminBatchId).get();
        }

        LOG.info("Saved {} and deleted {} non-patient resources for service {}", saved, deleted, serviceId);
        totalSaved += saved;
        totalDeleted += deleted;

        for (String patientId : patientBatchIdMap.keySet()) {
            UUID batchId = patientBatchIdMap.get(patientId);

            saved = countResourcesSaved.get(batchId).get();
            deleted = countResourcesDeleted.get(batchId).get();
            //LOG.info("Saved {} and deleted {} resources for patient {}", saved, deleted, edsPatientId);
            totalSaved += saved;
            totalDeleted += deleted;
        }

        LOG.info("CSV processing completed, saving {} resources and deleting {} over {} distinct patients for service {}", totalSaved, totalDeleted, patientBatchIdMap.size(), serviceId);
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

    /**
     * called when an exception occurs when processing a record in a CSV file, which stores the error in
     * a table which can then be used to re-play the transform for just those records that were in error
     */
    public void logTransformRecordError(Exception ex, CsvCurrentState state) {

        //if we've had more than 100 errors, don't bother logging or adding any more exceptions to the audit trail
        if (transformError.getError().size() > 100) {
            LOG.error("Error at " + state + ": " + ex.getMessage() + " (had over 100 exceptions, so not logging any more)");
            ex = null;

        } else {
            LOG.error("Error at " + state, ex);
        }

        //then add the error to our audit object
        Map<String, String> args = new HashMap<>();
        args.put(TransformErrorUtility.ARG_EMIS_CSV_DIRECTORY, state.getFileDir());
        args.put(TransformErrorUtility.ARG_EMIS_CSV_FILE, state.getFileName());
        args.put(TransformErrorUtility.ARG_EMIS_CSV_RECORD_NUMBER, "" + state.getRecordNumber());

        TransformErrorUtility.addTransformError(transformError, ex, args);
    }



    class MapAndSaveResourceTask implements Callable {

        private CsvCurrentState parserState = null;
        private Resource[] resources = null;
        private UUID batchUuid = null;
        private boolean isDelete = false;
        private boolean mapIds = false;

        public MapAndSaveResourceTask(CsvCurrentState parserState, UUID batchUuid, boolean isDelete, boolean mapIds, Resource... resources) {
            this.parserState = parserState;
            this.resources = resources;
            this.batchUuid = batchUuid;
            this.isDelete = isDelete;
            this.mapIds = mapIds;
        }

        @Override
        public Object call() throws Exception {

            for (Resource resource: resources) {

                try {
                    boolean isNewResource = false;
                    if (mapIds) {
                        isNewResource = IdHelper.mapIds(serviceId, systemId, resource);
                    }

                    if (isDelete) {
                        storageService.exchangeBatchDelete(exchangeId, batchUuid, resource);
                    } else {
                        storageService.exchangeBatchUpdate(exchangeId, batchUuid, resource, isNewResource);
                    }

                } catch (Exception ex) {
                    throw new TransformException("Exception mapping or storing " + resource.getResourceType() + " " + resource.getId(), ex);
                }
            }

            return null;
        }

        public CsvCurrentState getParserState() {
            return parserState;
        }
    }




}
