package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonResourceAccessor;
import org.endeavourhealth.core.data.ehr.models.EventStoreMode;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.data.ehr.models.PersonResourceEventStore;

import java.util.Date;
import java.util.UUID;

public class PersonResourceRepository extends Repository {
    public void insert(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        save(personResource, EventStoreMode.insert);
    }

    public void update(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        save(personResource, EventStoreMode.update);
    }

    private void save(PersonResource personResource, EventStoreMode mode){
        Mapper<PersonResource> mapperPersonResource = getMappingManager().mapper(PersonResource.class);
        Mapper<PersonResourceEventStore> mapperEventStore = getMappingManager().mapper(PersonResourceEventStore.class);

        PersonResourceEventStore eventStore = createEventStoreObject(personResource, mode);

        BatchStatement batch = new BatchStatement()
                .add(mapperPersonResource.saveQuery(personResource))
                .add(mapperEventStore.saveQuery(eventStore));

        getSession().execute(batch);
    }

    public void delete(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        Mapper<PersonResource> mapperPersonResource = getMappingManager().mapper(PersonResource.class);
        Mapper<PersonResourceEventStore> mapperEventStore = getMappingManager().mapper(PersonResourceEventStore.class);

        PersonResourceEventStore eventStore = createEventStoreObject(personResource, EventStoreMode.delete);

        BatchStatement batch = new BatchStatement()
                .add(mapperPersonResource.deleteQuery(personResource))
                .add(mapperEventStore.saveQuery(eventStore));

        getSession().execute(batch);
    }

    public PersonResource getByKey(UUID personId, String resourceType, UUID serviceId, UUID systemInstanceId, String resourceId) {

        Mapper<PersonResource> mapperPersonResource = getMappingManager().mapper(PersonResource.class);
        return mapperPersonResource.get(personId, resourceType, serviceId, systemInstanceId, resourceId);
    }

    public Iterable<PersonResource> getByResourceType(UUID personId, String resourceType) {

        PersonResourceAccessor accessor = getMappingManager().createAccessor(PersonResourceAccessor.class);
        return accessor.getByResourceType(personId, resourceType);
    }

    public Iterable<PersonResource> getByService(UUID personId, String resourceType, UUID serviceId) {

        PersonResourceAccessor accessor = getMappingManager().createAccessor(PersonResourceAccessor.class);
        return accessor.getByService(personId, resourceType, serviceId);
    }

    public Iterable<PersonResource> getByServiceInstance(UUID personId, String resourceType, UUID serviceId, UUID systemInstanceId) {

        PersonResourceAccessor accessor = getMappingManager().createAccessor(PersonResourceAccessor.class);
        return accessor.getBySystemInstance(personId, resourceType, serviceId, systemInstanceId);
    }

    private PersonResourceEventStore createEventStoreObject(PersonResource personResource, EventStoreMode mode) {
        Date createdTime = new Date();

        PersonResourceEventStore eventStore = new PersonResourceEventStore();
        eventStore.setPersonId(personResource.getPersonId());
        eventStore.setResourceType(personResource.getResourceType());
        eventStore.setServiceId(personResource.getServiceId());
        eventStore.setSystemInstanceId(personResource.getSystemInstanceId());
        eventStore.setResourceId(personResource.getResourceId());
        eventStore.setVersion(personResource.getVersion());
        eventStore.setCreated(createdTime);
        eventStore.setMode(mode);
        eventStore.setResourceMetadata(personResource.getResourceMetadata());
        eventStore.setSchemaVersion(personResource.getSchemaVersion());
        eventStore.setResourceData(personResource.getResourceData());
        return eventStore;
    }

}
