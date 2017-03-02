package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class UpdateStatementBuilder extends BoundStatementBuilder {

    private final FieldAndValueList columnValues = new FieldAndValueList();
    private final FieldAndValueList parameters = new FieldAndValueList();
    private final Clause[] clauses;

    public UpdateStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table, Clause clause) {
        this(statementCache, keyspace, table, new Clause[] { clause} );
    }

    public UpdateStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table, Clause[] clauses) {
        super(statementCache, keyspace, table);
        this.clauses = clauses;
    }

    public UpdateStatementBuilder addColumnUUID(String columnName, UUID value) {
        columnValues.add(FieldType.UUID, columnName, value);
        addParameterUUID(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addColumnString(String columnName, String value) {
        columnValues.add(FieldType.String, columnName, value);
        addParameterString(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addColumnInteger(String columnName, Integer value) {
        columnValues.add(FieldType.Integer, columnName, value);
        addParameterInteger(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addColumnBoolean(String columnName, Boolean value) {
        columnValues.add(FieldType.Boolean, columnName, value);
        addParameterBoolean(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addColumnTimestamp(String columnName, Date value) {
        columnValues.add(FieldType.Timestamp, columnName, value);
        addParameterTimestamp(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addColumnMap(String columnName, Map<String, String> value) {
        columnValues.add(FieldType.Map, columnName, value);
        addParameterMap(columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterUUID(String columnName, UUID value) {
        parameters.add(FieldType.UUID, columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterString(String columnName, String value) {
        parameters.add(FieldType.String, columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterInteger(String columnName, Integer value) {
        parameters.add(FieldType.Integer, columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterBoolean(String columnName, Boolean value) {
        parameters.add(FieldType.Boolean, columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterTimestamp(String columnName, Date value) {
        parameters.add(FieldType.Timestamp, columnName, value);
        return this;
    }

    public UpdateStatementBuilder addParameterMap(String columnName, Map<String, String> value) {
        parameters.add(FieldType.Map, columnName, value);
        return this;
    }

    public BoundStatement build() {

        Update update = QueryBuilder.update(super.keyspace, super.table);

        Update.Assignments assignments = null;

        for (FieldAndValue field: columnValues) {

            Assignment assignment = QueryBuilder.set(field.getColumnName(), QueryBuilder.bindMarker(field.getColumnName()));

            if (assignments == null)
                assignments = update.with(assignment);
            else
                assignments = assignments.and(assignment);
        }

        Update.Where where = assignments.where(clauses[0]);

        //Start at 1
        for (int i = 1; i < clauses.length; i++) {
            where = where.and(clauses[i]);
        }

        return super.build (where, parameters);
    }
}