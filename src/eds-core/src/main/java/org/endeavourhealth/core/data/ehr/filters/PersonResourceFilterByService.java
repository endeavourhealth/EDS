package org.endeavourhealth.core.data.ehr.filters;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.PreparedStatementCache;

import java.util.UUID;

public class PersonResourceFilterByService implements PersonResourceFilter {
    private UUID personId;
    private String resourceType;
    private UUID serviceId;

    public UUID getPersonId() {
        return personId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getServiceId() { return serviceId; }

    public PersonResourceFilterByService(UUID personId, String resourceType, UUID serviceId) {
        this.personId = personId;
        this.resourceType = resourceType;
        this.serviceId = serviceId;
    }

    @Override
    public BoundStatement toStatement(PreparedStatementCache statementCache, String keyspace, String table) {
        RegularStatement regularStatement = QueryBuilder.select()
                .all()
                .from(keyspace, table)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                    .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")))
                    .and(QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")));

        PreparedStatement preparedStatement = statementCache.getOrAdd(regularStatement);

        return preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType)
                .setUUID("service_id", serviceId);
    }
}
