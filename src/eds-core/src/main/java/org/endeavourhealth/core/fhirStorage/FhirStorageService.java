package org.endeavourhealth.core.fhirStorage;

import com.datastax.driver.core.utils.UUIDs;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceEntry;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.fhirStorage.exceptions.SerializationException;
import org.endeavourhealth.core.fhirStorage.exceptions.UnprocessableEntityException;
import org.endeavourhealth.core.fhirStorage.metadata.MetadataFactory;
import org.endeavourhealth.core.fhirStorage.metadata.PatientCompartment;
import org.endeavourhealth.core.fhirStorage.metadata.ResourceMetadata;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.eds.PatientSearchHelper;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class FhirStorageService {
    private static final String SCHEMA_VERSION = "0.1";

    private final ResourceRepository repository;
    //private final PatientIdentifierRepository identifierRepository;

    private final UUID serviceId;
    private final UUID systemId;

    public FhirStorageService(UUID serviceId, UUID systemId) {
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.repository = new ResourceRepository();
        //identifierRepository = new PatientIdentifierRepository();
    }

    public FhirResponse exchangeBatchUpdate(UUID exchangeId, UUID batchId, Resource resource, boolean isNewResource) throws Exception {
        store(resource, exchangeId, batchId, isNewResource);
        return new FhirResponse(resource);
    }

    /*public FhirResponse versionSpecificUpdate(UUID versionkey, Resource resource) throws Exception {
        Validate.resourceId(resource);
        Validate.hasVersion(versionkey);

        UUID resourceId = UUID.fromString(resource.getId());
        String resourceType = resource.getResourceType().toString();

        ResourceHistory current =  repository.getCurrentVersion(resourceType, resourceId);
        if (current == null) {
            throw new UnprocessableEntityException(String.format("Resource not found: ResourceType='%s', ResourceId='%s'", resourceType,resourceId.toString()));
        }

        Validate.isSameVersion(current.getVersion(), versionkey);

        store(resource, null, null);

        return new FhirResponse(resource);
    }*/

    public FhirResponse delete(Resource resource) throws UnprocessableEntityException, SerializationException {
        delete(resource, null, null);

        return new FhirResponse(resource);
    }

    public FhirResponse exchangeBatchDelete(UUID exchangeId, UUID batchId, Resource resource) throws UnprocessableEntityException, SerializationException {
        delete(resource, exchangeId, batchId);
        return new FhirResponse(resource);
    }


    private void store(Resource resource, UUID exchangeId, UUID batchId, boolean isNewResource) throws Exception {
        Validate.resourceId(resource);

        ResourceEntry entry = createResourceEntry(resource, exchangeId, batchId);

        //if we're updating a resource but there's no change, don't commit the save
        //this is because Emis send us up to thousands of duplicated resources each day
        if (!shouldSaveResource(entry, isNewResource)) {
            return;
        }

        FhirResourceHelper.updateMetaTags(resource, entry.getVersion(), entry.getCreatedAt());

        repository.save(entry);

        //call out to our patient search and person matching services
        if (resource instanceof Patient) {
            PatientLinkHelper.updatePersonId((Patient)resource);
            PatientSearchHelper.update(serviceId, systemId, (Patient)resource);

        } else if (resource instanceof EpisodeOfCare) {
            PatientSearchHelper.update(serviceId, systemId, (EpisodeOfCare)resource);
        }

        /*if (resource instanceof Patient) {
            identifierRepository.savePatientIdentity((Patient)resource, serviceId, systemId);
        }*/
    }

    private boolean shouldSaveResource(ResourceEntry entry, boolean isNewResource) throws Exception {

        //if it's a brand new resource, we always want to save it
        if (isNewResource) {
            return true;
        }

        //check the checksum first, so we only do a very small read from the DB
        Long previousChecksum = repository.getResourceChecksum(entry.getResourceType(), entry.getResourceId());
        if (previousChecksum == null
                || previousChecksum.longValue() != entry.getResourceChecksum()) {
            //if we don't have a previous checksum (which can happen if we keep re-running transforms
            //that fail, because it thinks it's not a new resource when it actually is) or the checksum differs,
            //then we want to save this resource
            return true;
        }

        //if the checksum is the same, we need to do a full compare
        ResourceHistory previousVersion = repository.getCurrentVersion(entry.getResourceType(), entry.getResourceId());

        //if it was previously deleted, or for some reason we didn't
        if (previousVersion == null
            || previousVersion.getIsDeleted()) {
            return true;
        }

        String previousData = previousVersion.getResourceData();
        if (previousData == null
            || entry.getResourceData()== null
            || !previousData.equals(entry.getResourceData())) {
            return true;
        }

        //if we get here, then the resource we're trying to save is completely identical to the last instance
        //of that same resource we previously saved to the DB, so don't save it again
        return false;
    }

    private void delete(Resource resource, UUID exchangeId, UUID batchId) throws UnprocessableEntityException, SerializationException {
        Validate.resourceId(resource);

        ResourceEntry entry = createResourceEntry(resource, exchangeId, batchId);
        repository.delete(entry);
    }

    private ResourceEntry createResourceEntry(Resource resource, UUID exchangeId, UUID batchId) throws UnprocessableEntityException, SerializationException {
        ResourceMetadata metadata = MetadataFactory.createMetadata(resource);
        Date entryDate = new Date();
        String resourceJson = FhirSerializationHelper.serializeResource(resource);

        ResourceEntry entry = new ResourceEntry();
        entry.setResourceId(FhirResourceHelper.getResourceId(resource));
        entry.setResourceType(FhirResourceHelper.getResourceType(resource));
        entry.setVersion(createTimeBasedVersion());
        entry.setCreatedAt(entryDate);
        entry.setServiceId(serviceId);
        entry.setSystemId(systemId);
        entry.setSchemaVersion(SCHEMA_VERSION);
        entry.setResourceMetadata(JsonSerializer.serialize(metadata));
        entry.setResourceData(resourceJson);
        entry.setResourceChecksum(generateChecksum(resourceJson));
        entry.setExchangeId(exchangeId);
        entry.setBatchId(batchId);

        if (metadata instanceof PatientCompartment) {
            entry.setPatientId(((PatientCompartment) metadata).getPatientId());
        }

        return entry;
    }

    private UUID createTimeBasedVersion() {
        return UUIDs.timeBased();
    }

    public static long generateChecksum(String data) {
        byte[] bytes = data.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }

}
