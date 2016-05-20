package org.endeavourhealth.core.data.ehr.filters;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.PreparedStatementCache;

import java.util.UUID;

public class PersonResourceFilterBySystemInstance implements PersonResourceFilter {
    private UUID personId;
    private String resourceType;
    private UUID serviceId;
    private UUID systemInstanceId;

    public UUID getPersonId() {
        return personId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getServiceId() { return serviceId; }

    public UUID getSystemInstanceId() { return systemInstanceId; }

    public PersonResourceFilterBySystemInstance(UUID personId, String resourceType, UUID serviceId, UUID systemInstanceId) {
        this.personId = personId;
        this.resourceType = resourceType;
        this.serviceId = serviceId;
        this.systemInstanceId = systemInstanceId;
    }

    @Override
    public BoundStatement toStatement(PreparedStatementCache statementCache, String keyspace, String table) {
        RegularStatement regularStatement = QueryBuilder.select()
                .all()
                .from(keyspace, table)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                    .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")))
                    .and(QueryBuilder.eq("service_id", QueryBuilder.bindMarker("service_id")))
                    .and(QueryBuilder.eq("system_instance_id", QueryBuilder.bindMarker("system_instance_id")));

        PreparedStatement preparedStatement = statementCache.getOrAdd(regularStatement);

        return preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType)
                .setUUID("service_id", serviceId)
                .setUUID("system_instance_id", systemInstanceId);
    }
}
