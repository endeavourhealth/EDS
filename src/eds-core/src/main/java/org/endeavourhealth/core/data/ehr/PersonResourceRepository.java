package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonResourceAccessor;
import org.endeavourhealth.core.data.EventStoreMode;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.data.ehr.models.PersonResourceEventStore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PersonResourceRepository extends Repository {

    public void insert(PersonResource personResource){
        if (personResource == null) {
            throw new IllegalArgumentException("personResource is null");
        }
        insert(Lists.newArrayList(personResource));
    }

    public void update(PersonResource personResource){
        if (personResource == null) {
            throw new IllegalArgumentException("personResource is null");
        }

        delete(Lists.newArrayList(personResource));
    }

    public void insert(List<PersonResource> personResources){
        save(personResources, EventStoreMode.insert);
    }

    public void delete(List<PersonResource> personResources){
        save(personResources, EventStoreMode.delete);
    }

    private void save(List<PersonResource> personResources, EventStoreMode storeMode){
        Mapper<PersonResource> mapperPersonResource = getMappingManager().mapper(PersonResource.class);
        Mapper<PersonResourceEventStore> mapperEventStore = getMappingManager().mapper(PersonResourceEventStore.class);

        BatchStatement batch = new BatchStatement();

        for (PersonResource personResource: personResources) {
            PersonResourceEventStore eventStore = createEventStoreObject(personResource, storeMode);

            switch (storeMode) {
                case insert:
                    batch.add(mapperPersonResource.saveQuery(personResource));
                    break;
                case update:
                    batch.add(mapperPersonResource.saveQuery(personResource)); //updates use saveQuery(..) as well as inserts
                    break;
                case delete:
                    batch.add(mapperPersonResource.deleteQuery(personResource));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid store mode " + storeMode);
            }

            //insert the audit entity
            batch.add(mapperEventStore.saveQuery(eventStore));

        }

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
