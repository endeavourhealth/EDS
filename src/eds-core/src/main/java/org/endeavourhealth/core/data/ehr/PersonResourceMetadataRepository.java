package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.filters.PersonResourceMetadataFilter;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.data.ehr.models.PersonResourceMetadata;
import org.endeavourhealth.core.utility.StreamExtension;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PersonResourceMetadataRepository extends Repository {
    private static final String KEYSPACE = "ehr";
    private static final String PERSON_RESOURCE_METADATA_TABLE = "person_resource_metadata_by_date";

    public PersonResourceMetadata getByKey(UUID personId,
            String resourceType,
            Date effectiveDate,
            UUID serviceId,
            UUID systemInstanceId,
            String resourceId) {
        PreparedStatement preparedStatement = getStatementCache().getOrAdd(QueryBuilder.select()
                .all()
                .from(KEYSPACE, PERSON_RESOURCE_METADATA_TABLE)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")))
                .and(QueryBuilder.eq("effective_date", QueryBuilder.bindMarker("effective_date")))
                .and(QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")))
                .and(QueryBuilder.eq("system_instance_id", QueryBuilder.bindMarker("system_instance_id")))
                .and(QueryBuilder.eq("resource_id", QueryBuilder.bindMarker("resource_id"))));

        BoundStatement boundStatement = preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType)
                .setTimestamp("effective_date", effectiveDate)
                .setUUID("service_id", serviceId)
                .setUUID("system_instance_id", systemInstanceId)
                .setString("resource_id", resourceId);

        return getSession().execute(boundStatement)
                .all()
                .stream()
                .map(this::mapRowToPersonResourceMetadata)
                .collect(StreamExtension.singleOrNullCollector());
    }

    public List<PersonResourceMetadata> getApplyFilter(PersonResourceMetadataFilter filter) {
        BoundStatement boundStatement = filter.toStatement(getStatementCache(), KEYSPACE, PERSON_RESOURCE_METADATA_TABLE);

        return getSession().execute(boundStatement)
                .all()
                .stream()
                .map(this::mapRowToPersonResourceMetadata)
                .collect(Collectors.toList());
    }

    private PersonResourceMetadata mapRowToPersonResourceMetadata(Row row) {
        PersonResourceMetadata resourceMetadata = new PersonResourceMetadata();
        resourceMetadata.setPersonId(row.getUUID("person_id"));
        resourceMetadata.setResourceType(row.getString("resource_type"));
        resourceMetadata.setEffectiveDate(row.getTimestamp("effective_date"));
        resourceMetadata.setServiceId(row.getUUID("service_id"));
        resourceMetadata.setSystemInstanceId(row.getUUID("system_instance_id"));
        resourceMetadata.setResourceId(row.getString("resource_id"));
        resourceMetadata.setResourceMetadata(row.getString("resource_metadata"));
        return resourceMetadata;
    }
}
