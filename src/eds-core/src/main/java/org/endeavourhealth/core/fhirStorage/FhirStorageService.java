package org.endeavourhealth.core.fhirStorage;

import com.datastax.driver.core.utils.UUIDs;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceEntry;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.fhirStorage.exceptions.SerializationException;
import org.endeavourhealth.core.fhirStorage.exceptions.UnprocessableEntityException;
import org.endeavourhealth.core.fhirStorage.exceptions.VersionConflictException;
import org.endeavourhealth.core.fhirStorage.metadata.PatientCompartment;
import org.endeavourhealth.core.fhirStorage.metadata.ResourceMetadata;
import org.endeavourhealth.core.fhirStorage.metadata.MetadataFactory;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FhirStorageService {
    private static final String SCHEMA_VERSION = "1.0";

    private final ResourceRepository repository;

    private final UUID serviceId;
    private final UUID systemId;

    public FhirStorageService(UUID serviceId, UUID systemId) {
        this.serviceId = serviceId;
        this.systemId = systemId;
        this.repository = new ResourceRepository();
    }

    public FhirResponse update(Resource resource) throws UnprocessableEntityException, SerializationException {
        store(resource, null, null);

        return new FhirResponse(resource);
    }

    public FhirResponse exchangeBatchUpdate(UUID exchangeId, UUID batchId, List<Resource> resources) throws UnprocessableEntityException, SerializationException {
        for (Resource resource :resources) {
            store(resource, exchangeId, batchId);
        }

        return new FhirResponse(resources);
    }

    public FhirResponse versionSpecificUpdate(UUID versionkey, Resource resource) throws VersionConflictException, UnprocessableEntityException, SerializationException {
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
    }

    public FhirResponse delete(Resource resource) throws UnprocessableEntityException, SerializationException {
        delete(resource, null, null);

        return new FhirResponse(resource);
    }

    public FhirResponse exchangeBatchDelete(UUID exchangeId, UUID batchId, List<Resource> resources) throws UnprocessableEntityException, SerializationException {
        for (Resource resource :resources) {
            delete(resource, exchangeId, batchId);
        }

        return new FhirResponse(resources);
    }

    private void store(Resource resource, UUID exchangeId, UUID batchId) throws UnprocessableEntityException, SerializationException {
        Validate.resourceId(resource);

        ResourceEntry entry = createResourceEntry(resource);

        updateResourceMeta(resource, entry.getVersion(), entry.getCreatedAt());

        repository.save(entry, exchangeId, batchId);
    }

    private void delete(Resource resource, UUID exchangeId, UUID batchId) throws UnprocessableEntityException, SerializationException {
        Validate.resourceId(resource);

        ResourceEntry entry = createResourceEntry(resource);

        repository.delete(entry, exchangeId, batchId);
    }

    private ResourceEntry createResourceEntry(Resource resource) throws UnprocessableEntityException, SerializationException {
        ResourceMetadata metadata = MetadataFactory.createMetadata(resource);

        ResourceEntry entry = new ResourceEntry();
        entry.setResourceId(metadata.getId());
        entry.setResourceType(metadata.getResourceTypeName());
        entry.setVersion(UUIDs.timeBased());
        entry.setCreatedAt(new Date());
        entry.setServiceId(serviceId);
        entry.setSystemId(systemId);
        entry.setSchemaVersion(SCHEMA_VERSION);
        entry.setResourceData(JsonSerializationHelper.serializeResource(resource));

        if (metadata instanceof PatientCompartment) {
            entry.setPatientId(((PatientCompartment) metadata).getPatientId());
        }

        return entry;
    }

    private void updateResourceMeta(Resource resource, UUID version, Date createdAt) {
        Meta meta = resource.getMeta();
        meta.setVersionId(version.toString());
        meta.setLastUpdated(createdAt);
    }
}
