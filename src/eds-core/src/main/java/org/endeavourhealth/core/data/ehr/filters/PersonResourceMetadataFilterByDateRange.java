package org.endeavourhealth.core.data.ehr.filters;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.endeavourhealth.core.data.PreparedStatementCache;

import java.util.Date;
import java.util.UUID;

public class PersonResourceMetadataFilterByDateRange implements PersonResourceMetadataFilter {
    private UUID personId;
    private String resourceType;
    private Date startDate;
    private Date endDate;

    public UUID getPersonId() { return personId; }

    public String getResourceType() { return resourceType; }

    public Date getStartDate() { return startDate; }

    public Date getEndDate() { return endDate; }

    public PersonResourceMetadataFilterByDateRange(UUID personId, String resourceType, Date startDate, Date endDate) {
        this.personId = personId;
        this.resourceType = resourceType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public BoundStatement toStatement(PreparedStatementCache statementCache, String keyspace, String table) {
        RegularStatement regularStatement = QueryBuilder.select()
                .all()
                .from(keyspace, table)
                .where(QueryBuilder.eq("person_id", QueryBuilder.bindMarker("person_id")))
                    .and(QueryBuilder.eq("resource_type", QueryBuilder.bindMarker("resource_type")))
                    .and(QueryBuilder.gte("effective_date", QueryBuilder.bindMarker("effective_date_start")))
                    .and(QueryBuilder.lte("effective_date", QueryBuilder.bindMarker("effective_date_end")));

        PreparedStatement preparedStatement = statementCache.getOrAdd(regularStatement);

        return preparedStatement
                .bind()
                .setUUID("person_id", personId)
                .setString("resource_type", resourceType)
                .setTimestamp("effective_date_start", startDate)
                .setTimestamp("effective_date_end", endDate);
    }
}
