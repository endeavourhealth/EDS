package org.endeavourhealth.transform.common;

import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceByService;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.Resource;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * takes some resources, computes the delta to what's on the DB already and files them
 */
public class FhirDeltaResourceFilter {

    private final int maxThreadsToUse;
    private final UUID serviceId;
    private final UUID systemId;
    private ResourceRepository resourceRepository = new ResourceRepository();

    public FhirDeltaResourceFilter(UUID serviceId, UUID systemId, int maxFilingThreads) {
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.maxThreadsToUse = maxFilingThreads;
    }

    public void process(List<Resource> resources, UUID exchangeId, TransformError currentErrors, List<UUID> batchIdsToPopulate) throws Exception {

        //first the IDs must be mapped, so we can compare to what's on the DB already
        mapIds(resources);

        //check for what resources are duplicates
        //note: we can't work out if admin resources should be deleted, since the admin resources
        //may be referenced by any patient in the same service/system, not just the one(s) being saved here
        List<Resource> adminUpserts = new ArrayList<>();
        List<Resource> patientUpserts = new ArrayList<>();
        List<Resource> patientDeletes = new ArrayList<>();
        filterForDelta(resources, adminUpserts, patientUpserts, patientDeletes);

        //then file what's left
        fileResources(exchangeId, currentErrors, adminUpserts, patientUpserts, patientDeletes, batchIdsToPopulate);

    }

    private void fileResources(UUID exchangeId, TransformError currentErrors, List<Resource> adminUpserts,
                               List<Resource> patientUpserts,  List<Resource> patientDeletes,
                               List<UUID> batchIdsToPopulate) throws Exception {

        FhirResourceFiler filer = new FhirResourceFiler(exchangeId, serviceId, systemId, currentErrors, batchIdsToPopulate, maxThreadsToUse);

        for (Resource resource: adminUpserts) {
            filer.saveAdminResource(null, false, resource);
        }

        for (Resource resource: patientUpserts) {
            String patientId = IdHelper.getPatientId(resource);
            filer.savePatientResource(null, false, patientId, resource);
        }

        for (Resource resource: patientDeletes) {
            String patientId = IdHelper.getPatientId(resource);
            filer.deletePatientResource(null, false, patientId, resource);
        }

        //must wait for everything to finish
        filer.waitToFinish();
    }

    private void filterForDelta(List<Resource> resources, List<Resource> adminUpserts,
                                List<Resource> patientUpserts,  List<Resource> patientDeletes) throws Exception {

        //hash the patient resources by patient ID and separate out the admin ones
        List<Resource> adminResources = new ArrayList<>();
        Map<String, List<Resource>> hmPatientResources = new HashMap<>();

        for (Resource resource: resources) {

            if (FhirResourceFiler.isPatientResource(resource)) {

                String patientId = IdHelper.getPatientId(resource);

                List<Resource> patientResources = hmPatientResources.get(patientId);
                if (patientResources == null) {
                    patientResources = new ArrayList<>();
                    hmPatientResources.put(patientId, patientResources);
                }
                patientResources.add(resource);

            } else {
                adminResources.add(resource);
            }
        }

        //process the admin resources
        filterAdminResourcesForDelta(adminResources, adminUpserts);

        //process each patient's resources
        for (Map.Entry<String, List<Resource>> entry : hmPatientResources.entrySet())
            filterPatientResourcesForDelta(entry.getKey(), entry.getValue(), patientUpserts, patientDeletes);
    }

    private void filterAdminResourcesForDelta(List<Resource> adminResources, List<Resource> adminUpserts) throws Exception {

        //hash by resource types
        HashMap<String, List<Resource>> hmResourceTypes = new HashMap<>();
        for (Resource resource: adminResources) {

            String resourceType = resource.getResourceType().toString();
            List<Resource> resourcesOfType = hmResourceTypes.get(resourceType);
            if (resourcesOfType == null) {
                resourcesOfType = new ArrayList<>();
                hmResourceTypes.put(resourceType, resourcesOfType);
            }
            resourcesOfType.add(resource);
        }

        for (Map.Entry<String, List<Resource>> entry : hmResourceTypes.entrySet()) {
            List<Resource> resourcesOfType = entry.getValue();
            //retrieve all the resources of this type for the service and hash the JSON by ID
            HashMap<String, String> hmExistingResources = new HashMap<>();
            List<ResourceByService> existingResources = resourceRepository.getResourcesByService(serviceId, systemId, entry.getKey());
            for (ResourceByService existingResource: existingResources) {
                String id = existingResource.getResourceId().toString();
                String json = existingResource.getResourceData();
                hmExistingResources.put(id, json);
            }

            //go through our admin resources to see what we want to save and what's a duplicate
            for (Resource resource: resourcesOfType) {
                String resourceId = resource.getId();

                String existingJson = hmExistingResources.get(resourceId);
                if (existingJson == null) {
                    //if there's no json on the DB, then it definitely wants saving, as it's a new resource
                    adminUpserts.add(resource);

                } else {

                    String resourceJson = FhirSerializationHelper.serializeResource(resource);
                    if (!resourceJson.equals(existingJson)) {
                        //if the json differs to what's already on the DB, then we also want to save the resource
                        adminUpserts.add(resource);
                    }
                }
            }
        }
    }

    private void filterPatientResourcesForDelta(String patientId, List<Resource> patientResources, List<Resource> patientUpserts,
                                                   List<Resource> patientDeletes) throws Exception {

        //retrieve all existing resources on the DB for the patient and hash the json by resource ID
        HashMap<String, String> hmExistingResources = new HashMap<>();
        List<ResourceByPatient> existingResources = resourceRepository.getResourcesByPatient(serviceId, systemId, UUID.fromString(patientId));
        for (ResourceByPatient existingResource: existingResources) {
            String id = existingResource.getResourceId().toString();
            String json = existingResource.getResourceData();
            hmExistingResources.put(id, json);
        }

        //go through our patient resources to see what we want to save, what to delete and what to ignore
        for (Resource resource: patientResources) {
            String resourceId = resource.getId();

            String existingJson = hmExistingResources.remove(resourceId); //deliberate remove, since we check what's left
            if (existingJson == null) {
                //if there's no json on the DB, then it definitely wants saving, as it's a new resource
                patientUpserts.add(resource);

            } else {

                String resourceJson = FhirSerializationHelper.serializeResource(resource);
                if (!resourceJson.equals(existingJson)) {
                    //if the json differs to what's already on the DB, then we also want to save the resource
                    patientUpserts.add(resource);
                }
            }
        }

        //anything left in the hashmap is a resource that should be deleted
        for (String existingJson: hmExistingResources.values()) {
            Resource existingResource = FhirSerializationHelper.deserializeResource(existingJson);
            patientDeletes.add(existingResource);
        }

    }

    private void mapIds(List<Resource> resources) throws Exception {

        //don't set a limit on the pool to start blocking, since the resources
        //are already in memory, so having them all in the queue won't use much more
        ThreadPool idMappingPool = new ThreadPool(maxThreadsToUse, Integer.MAX_VALUE);

        for (Resource resource: resources) {

            MapIdTask callable = new MapIdTask(resource);
            List<ThreadPoolError> errors = idMappingPool.submit(callable);
            handleErrors(errors);
        }

        List<ThreadPoolError> errors = idMappingPool.waitAndStop();
        handleErrors(errors);
    }
    /*private void mapIds(List<Resource> resources) throws Exception {

        for (Resource resource: resources) {
            try {
                IdHelper.mapIds(serviceId, systemId, resource);
            } catch (Exception ex) {
                throw new TransformException("Exception mapping  " + resource.getResourceType() + " " + resource.getId(), ex);
            }
        }
    }*/

    private void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple exceptions, this will only log the first, but the first exception is the one that's interesting
        for (ThreadPoolError error: errors) {
            Exception exception = error.getException();
            throw exception;
        }
    }


    /**
     * thread pool runnable to map resource IDs and references in parallel
     */
    class MapIdTask implements Callable {

        private Resource resource = null;

        public MapIdTask(Resource resource) {
            this.resource = resource;
        }

        @Override
        public Object call() throws Exception {

            try {
                IdHelper.mapIds(serviceId, systemId, resource);
            } catch (Exception ex) {
                throw new TransformException("Exception mapping  " + resource.getResourceType() + " " + resource.getId(), ex);
            }

            return null;
        }
    }

}
