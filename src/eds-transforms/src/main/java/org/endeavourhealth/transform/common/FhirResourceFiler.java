package org.endeavourhealth.transform.common;

import com.datastax.driver.core.utils.UUIDs;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.fhirStorage.FhirStorageService;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class FhirResourceFiler {

    private static final Logger LOG = LoggerFactory.getLogger(FhirResourceFiler.class);

    private static Set<ResourceType> patientResourceTypes = null;

    private final UUID exchangeId;
    private final UUID serviceId;
    private final UUID systemId;
    private final FhirStorageService storageService;
    private final ExchangeBatchRepository exchangeBatchRepository;
    private final TransformError transformError;
    //private final ExchangeTransformAudit transformAudit;
    //private final Map<String, String> resourceTypes; //although a set would be idea, a map allows safe multi-thread access
    private final List<UUID> batchIdsCreated;

    //batch IDs
    private ReentrantLock batchIdLock = new ReentrantLock();
    private Map<String, ExchangeBatch> patientBatchIdMap = new ConcurrentHashMap<>();
    private ExchangeBatch adminBatchId = null;

    //threading
    private ThreadPool threadPool = null;

    //counts
    private Map<ExchangeBatch, AtomicInteger> countResourcesSaved = new ConcurrentHashMap<>();
    private Map<ExchangeBatch, AtomicInteger> countResourcesDeleted = new ConcurrentHashMap<>();


    public FhirResourceFiler(UUID exchangeId, UUID serviceId, UUID systemId, TransformError transformError,
                             List<UUID> batchIdsCreated, int maxFilingThreads) {
        this.exchangeId = exchangeId;
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.storageService = new FhirStorageService(serviceId, systemId);
        this.exchangeBatchRepository = new ExchangeBatchRepository();
        this.transformError = transformError;
        this.batchIdsCreated = batchIdsCreated;
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
                                    ExchangeBatch exchangeBatch,
                                    boolean toDelete,
                                    Resource... resources) throws Exception {

        for (Resource resource: resources) {
            //validate we're treating the resource properly as admin / patient
            if (isPatientResource(resource) != expectingPatientResource) {
                throw new PatientResourceException(resource, expectingPatientResource);
            }

            /*String resourceType = resource.getResourceType().toString();
            resourceTypes.put(resourceType, resourceType);*/

            //increment our counters for auditing
            if (toDelete) {
                countResourcesDeleted.get(exchangeBatch).incrementAndGet();
            } else {
                countResourcesSaved.get(exchangeBatch).incrementAndGet();
            }
        }

        List<ThreadPoolError> errors = threadPool.submit(new MapAndSaveResourceTask(parserState, toDelete, mapIds, exchangeBatch, expectingPatientResource, resources));
        handleErrors(errors);
    }

    private ExchangeBatch getAdminBatchId() {
        if (adminBatchId == null) {

            try {
                batchIdLock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to batchIdLock
                if (adminBatchId == null) {
                    adminBatchId = createExchangeBatch();
                }
            } finally {
                batchIdLock.unlock();
            }
        }
        return adminBatchId;
    }

    private ExchangeBatch getPatientBatchId(String patientId) {
        ExchangeBatch patientBatch = patientBatchIdMap.get(patientId);
        if (patientBatch == null) {

            try {
                batchIdLock.lock();

                //make sure to check if it's still null, as another thread may have created the ID while we were waiting to batchIdLock
                patientBatch = patientBatchIdMap.get(patientId);

                if (patientBatch == null) {
                    patientBatch = createExchangeBatch();
                    patientBatchIdMap.put(patientId, patientBatch);
                }
            } finally {
                batchIdLock.unlock();
            }
        }
        return patientBatch;
    }

    private ExchangeBatch createExchangeBatch() {
        ExchangeBatch exchangeBatch = new ExchangeBatch();
        exchangeBatch.setBatchId(UUIDs.timeBased());
        exchangeBatch.setExchangeId(exchangeId);
        exchangeBatch.setInsertedAt(new Date());
        exchangeBatchRepository.save(exchangeBatch);

        UUID batchId = exchangeBatch.getBatchId();
        batchIdsCreated.add(batchId);

        countResourcesDeleted.put(exchangeBatch, new AtomicInteger());
        countResourcesSaved.put(exchangeBatch, new AtomicInteger());

        return exchangeBatch;
    }

    /**
     * called after all content has been processed. It blocks until all operations have
     * been completed in the thread pool, then returns the distinct batch IDs created
     */
    public void waitToFinish() throws Exception {

        //wait for all tasks to be completed
        List<ThreadPoolError> errors = threadPool.waitAndStop();
        handleErrors(errors);

        //update the resource types used
        //saveResourceTypesUsed();

        //log out counts of what we processed
        logResults();

        //return getAllBatchIds();
    }

    private void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        for (ThreadPoolError error: errors) {

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


        int adminSaved = 0;
        int adminDeleted = 0;
        if (adminBatchId != null) {
            adminSaved += countResourcesSaved.get(adminBatchId).get();
            adminDeleted += countResourcesDeleted.get(adminBatchId).get();
        }

        int patientSaved = 0;
        int patientDeleted = 0;
        int patientCount = patientBatchIdMap.size();

        for (ExchangeBatch exchangeBatch : patientBatchIdMap.values()) {
            patientSaved += countResourcesSaved.get(exchangeBatch).get();
            patientDeleted += countResourcesDeleted.get(exchangeBatch).get();
        }

        LOG.info("Resource filing completed: admin resources [saved " + adminSaved + ", deleted " + adminDeleted + "]"
                + ", patient resources [saved " + patientSaved + ", deleted " + patientDeleted + " over " + patientCount + " patients]");
    }

    /*private List<UUID> getAllBatchIds() {
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
    }*/

    public static boolean isPatientResource(Resource resource) {
        return isPatientResource(resource.getResourceType());
    }

    public static boolean isPatientResource(ResourceType type) {

        if (patientResourceTypes == null) {
            Set<ResourceType> set = new HashSet<>();
            set.add(ResourceType.AllergyIntolerance);
            set.add(ResourceType.Appointment);
            set.add(ResourceType.Condition);
            set.add(ResourceType.DiagnosticOrder);
            set.add(ResourceType.DiagnosticReport);
            set.add(ResourceType.Encounter);
            set.add(ResourceType.EpisodeOfCare);
            set.add(ResourceType.FamilyMemberHistory);
            set.add(ResourceType.Immunization);
            set.add(ResourceType.MedicationOrder);
            set.add(ResourceType.MedicationStatement);
            set.add(ResourceType.Observation);
            set.add(ResourceType.Order);
            set.add(ResourceType.Patient);
            set.add(ResourceType.Procedure);
            set.add(ResourceType.ProcedureRequest);
            set.add(ResourceType.ReferralRequest);
            set.add(ResourceType.RelatedPerson);
            set.add(ResourceType.Specimen);

            //although Slot isn't technically linked to a patient, it is saved at the same time as
            //Appointment resources, so should be treated as one
            set.add(ResourceType.Slot);

            patientResourceTypes = set;
        }

        return patientResourceTypes.contains(type);
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
        //args.put(TransformErrorUtility.ARG_EMIS_CSV_DIRECTORY, state.getFileDir());
        args.put(TransformErrorUtility.ARG_EMIS_CSV_FILE, state.getFileName());
        args.put(TransformErrorUtility.ARG_EMIS_CSV_RECORD_NUMBER, "" + state.getRecordNumber());

        TransformErrorUtility.addTransformError(transformError, ex, args);
    }



    class MapAndSaveResourceTask implements Callable {

        private CsvCurrentState parserState = null;
        private boolean isDelete = false;
        private boolean mapIds = false;
        private ExchangeBatch exchangeBatch = null;
        private boolean patientResources = false;
        private Resource[] resources = null;

        public MapAndSaveResourceTask(CsvCurrentState parserState, boolean isDelete, boolean mapIds,
                                      ExchangeBatch exchangeBatch, boolean patientResources, Resource... resources) {
            this.parserState = parserState;
            this.isDelete = isDelete;
            this.mapIds = mapIds;
            this.exchangeBatch = exchangeBatch;
            this.patientResources = patientResources;
            this.resources = resources;
        }

        @Override
        public Object call() throws Exception {

            for (Resource resource: resources) {

                try {
                    boolean isNewResource = false;
                    if (mapIds) {
                        isNewResource = IdHelper.mapIds(serviceId, systemId, resource);
                    }

                    //if we've not set the EDS patient ID on our batch yet, then do so now
                    if (patientResources
                            && exchangeBatch.getEdsPatientId() == null) {
                        try {
                            String edsPatientId = IdHelper.getPatientId(resource);
                            exchangeBatch.setEdsPatientId(UUID.fromString(edsPatientId));
                            exchangeBatchRepository.save(exchangeBatch);
                        } catch (Exception ex) {
                            //if we try this on a Slot, it'll fail, so just ignore any errors
                        }
                    }

                    UUID batchUuid = exchangeBatch.getBatchId();
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
