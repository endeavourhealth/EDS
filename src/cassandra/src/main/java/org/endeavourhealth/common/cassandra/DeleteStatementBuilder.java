package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Date;
import java.util.UUID;

public class DeleteStatementBuilder extends BoundStatementBuilder {

    private final FieldAndValueList parameters = new FieldAndValueList();
    private final Clause[] clauses;

    public DeleteStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table, Clause clause) {
        this(statementCache, keyspace, table, new Clause[] { clause });
    }

    public DeleteStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table, Clause[] clauses) {
        super(statementCache, keyspace, table);
        this.clauses = clauses;
    }

    public DeleteStatementBuilder addParameterUUID(String columnName, UUID value) {
        parameters.add(FieldType.UUID, columnName, value);
        return this;
    }

    public DeleteStatementBuilder addParameterString(String columnName, String value) {
        parameters.add(FieldType.String, columnName, value);
        return this;
    }

    public DeleteStatementBuilder addParameterInteger(String columnName, Integer value) {
        parameters.add(FieldType.Integer, columnName, value);
        return this;
    }

    public DeleteStatementBuilder addParameterTimestamp(String columnName, Date value) {
        parameters.add(FieldType.Timestamp, columnName, value);
        return this;
    }

    public BoundStatement build() {

        Delete.Where delete = QueryBuilder.delete()
                .from(super.keyspace, super.table)
                .where(clauses[0]);

        //start at index 1
        for (int i = 1; i < clauses.length; i++) {
            delete = delete.and(clauses[i]);
        }

        return super.build(delete, parameters);
    }
}
