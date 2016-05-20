package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.DeleteStatementBuilder;
import org.endeavourhealth.core.data.InsertStatementBuilder;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.UpdateStatementBuilder;
import org.endeavourhealth.core.data.ehr.filters.PersonResourceFilter;
import org.endeavourhealth.core.data.ehr.models.EventStoreMode;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.utility.StreamExtension;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PersonResourceRepository extends Repository {
    private static final String KEYSPACE = "ehr";
    private static final String PERSON_RESOURCE_TABLE = "person_resource";
    private static final String PERSON_RESOURCE_EVENT_STORE_TABLE = "person_resource_event_store";

    public void insert(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        BoundStatement personResourceInsertStatement = new InsertStatementBuilder(getStatementCache(), KEYSPACE, PERSON_RESOURCE_TABLE)
                .addColumnUUID("person_id", personResource.getPersonId())
                .addColumnString("resource_type", personResource.getResourceType())
                .addColumnUUID("service_id", personResource.getServiceId())
                .addColumnUUID("system_instance_id", personResource.getSystemInstanceId())
                .addColumnString("resource_id", personResource.getResourceId())
                .addColumnTimestamp("effective_date", personResource.getEffectiveDate())
                .addColumnString("version", personResource.getVersion())
                .addColumnTimestamp("last_updated", personResource.getLastUpdated())
                .addColumnString("resource_metadata", personResource.getResourceMetadata())
                .addColumnString("schema_version", personResource.getSchemaVersion())
                .addColumnString("resource_data", personResource.getResourceData())
                .build();

        BoundStatement eventLogInsertStatement = buildEventLogInsertStatement(personResource, EventStoreMode.insert);

        BatchStatement batch = new BatchStatement()
                .add(personResourceInsertStatement)
                .add(eventLogInsertStatement);

        getSession().execute(batch);
    }

    public void update(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        Clause[] clauses = new Clause[]{
                QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")),
                QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")),
                QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")),
                QueryBuilder.eq("system_instance_id", QueryBuilder.bindMarker("system_instance_id")),
                QueryBuilder.eq("resource_id", QueryBuilder.bindMarker("resource_id"))
        };

        BoundStatement personResourceUpdateStatement = new UpdateStatementBuilder(getStatementCache(), KEYSPACE, PERSON_RESOURCE_TABLE, clauses)
                .addColumnTimestamp("effective_date", personResource.getEffectiveDate())
                .addColumnString("version", personResource.getVersion())
                .addColumnTimestamp("last_updated", personResource.getLastUpdated())
                .addColumnString("resource_metadata", personResource.getResourceMetadata())
                .addColumnString("schema_version", personResource.getSchemaVersion())
                .addColumnString("resource_data", personResource.getResourceData())
                .addParameterUUID("person_id", personResource.getPersonId())
                .addParameterString("resource_type", personResource.getResourceType())
                .addParameterUUID("service_id", personResource.getServiceId())
                .addParameterUUID("system_instance_id", personResource.getSystemInstanceId())
                .addParameterString("resource_id", personResource.getResourceId())
                .build();

        BoundStatement eventLogInsertStatement = buildEventLogInsertStatement(personResource, EventStoreMode.update);

        BatchStatement batch = new BatchStatement()
                .add(personResourceUpdateStatement)
                .add(eventLogInsertStatement);

        getSession().execute(batch);
    }

    public void delete(PersonResource personResource){
        if (personResource == null)
            throw new IllegalArgumentException("personResource is null");

        Clause[] clauses = new Clause[]{
                QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")),
                QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")),
                QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")),
                QueryBuilder.eq("system_instance_id", QueryBuilder.bindMarker("system_instance_id")),
                QueryBuilder.eq("resource_id", QueryBuilder.bindMarker("resource_id"))
        };

        BoundStatement personResourceDeleteStatement = new DeleteStatementBuilder(getStatementCache(), KEYSPACE, PERSON_RESOURCE_TABLE, clauses)
                .addParameterUUID("person_id", personResource.getPersonId())
                .addParameterString("resource_type", personResource.getResourceType())
                .addParameterUUID("service_id", personResource.getServiceId())
                .addParameterUUID("system_instance_id", personResource.getSystemInstanceId())
                .addParameterString("resource_id", personResource.getResourceId())
                .build();

        BoundStatement eventLogInsertStatement = buildEventLogInsertStatement(personResource, EventStoreMode.delete);

        BatchStatement batch = new BatchStatement()
                .add(personResourceDeleteStatement)
                .add(eventLogInsertStatement);

        getSession().execute(batch);
    }

    public PersonResource getByKey(UUID personId,
            String resourceType,
            UUID serviceId,
            UUID systemInstanceId,
            String resourceId) {
        PreparedStatement preparedStatement = getStatementCache().getOrAdd(QueryBuilder.select()
                .all()
                .from(KEYSPACE, PERSON_RESOURCE_TABLE)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")))
                .and(QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")))
                .and(QueryBuilder.eq("system_instance_id", QueryBuilder.bindMarker("system_instance_id")))
                .and(QueryBuilder.eq("resource_id", QueryBuilder.bindMarker("resource_id"))));

        BoundStatement boundStatement = preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType)
                .setUUID("service_id", serviceId)
                .setUUID("system_instance_id", systemInstanceId)
                .setString("resource_id", resourceId);

        return getSession().execute(boundStatement)
                .all()
                .stream()
                .map(this::mapRowToPersonResource)
                .collect(StreamExtension.singleOrNullCollector());
    }

    public List<PersonResource> getApplyFilter(PersonResourceFilter filter) {
        BoundStatement boundStatement = filter.toStatement(getStatementCache(), KEYSPACE, PERSON_RESOURCE_TABLE);

        return getSession().execute(boundStatement)
                .all()
                .stream()
                .map(this::mapRowToPersonResource)
                .collect(Collectors.toList());
    }

    private BoundStatement buildEventLogInsertStatement(PersonResource personResource, EventStoreMode mode) {
        Date createdTime = new Date();

        return new InsertStatementBuilder(getStatementCache(), KEYSPACE, PERSON_RESOURCE_EVENT_STORE_TABLE)
                .addColumnUUID("person_id", personResource.getPersonId())
                .addColumnString("resource_type", personResource.getResourceType())
                .addColumnUUID("service_id", personResource.getServiceId())
                .addColumnUUID("system_instance_id", personResource.getSystemInstanceId())
                .addColumnString("resource_id", personResource.getResourceId())
                .addColumnString("version", personResource.getVersion())
                .addColumnTimestamp("created", createdTime)
                .addColumnString("mode", mode.toString())
                .addColumnString("resource_metadata", personResource.getResourceMetadata())
                .addColumnString("schema_version", personResource.getSchemaVersion())
                .addColumnString("resource_data", personResource.getResourceData())
                .build();
    }

    private PersonResource mapRowToPersonResource(Row row) {
        PersonResource resource = new PersonResource();
        resource.setPersonId(row.getUUID("person_id"));
        resource.setResourceType(row.getString("resource_type"));
        resource.setServiceId(row.getUUID("service_id"));
        resource.setSystemInstanceId(row.getUUID("system_instance_id"));
        resource.setResourceId(row.getString("resource_id"));
        resource.setEffectiveDate(row.getTimestamp("effective_date"));
        resource.setVersion(row.getString("version"));
        resource.setLastUpdated(row.getTimestamp("last_updated"));
        resource.setResourceMetadata(row.getString("resource_metadata"));
        resource.setSchemaVersion(row.getString("schema_version"));
        resource.setResourceData(row.getString("resource_data"));
        return resource;
    }
}
