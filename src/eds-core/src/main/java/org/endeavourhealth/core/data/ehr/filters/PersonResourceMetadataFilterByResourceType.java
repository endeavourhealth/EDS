package org.endeavourhealth.core.data.ehr.filters;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.PreparedStatementCache;

import java.util.UUID;

public class PersonResourceMetadataFilterByResourceType implements PersonResourceMetadataFilter {
    private UUID personId;
    private String resourceType;

    public UUID getPersonId() { return personId; }

    public String getResourceType() { return resourceType; }

    public PersonResourceMetadataFilterByResourceType(UUID personId, String resourceType) {
        this.personId = personId;
        this.resourceType = resourceType;
    }

    @Override
    public BoundStatement toStatement(PreparedStatementCache statementCache, String keyspace, String table) {
        RegularStatement regularStatement = QueryBuilder.select()
                .all()
                .from(keyspace, table)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                    .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")));

        PreparedStatement preparedStatement = statementCache.getOrAdd(regularStatement);

        return preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType);
    }
}
