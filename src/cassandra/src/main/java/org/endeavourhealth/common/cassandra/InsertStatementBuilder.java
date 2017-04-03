package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class InsertStatementBuilder extends BoundStatementBuilder {

    protected final FieldAndValueList columnValues = new FieldAndValueList();

    public InsertStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table) {
        super(statementCache, keyspace, table);
    }

    public InsertStatementBuilder addColumnUUID(String columnName, UUID value) {
        columnValues.add(FieldType.UUID, columnName, value);
        return this;
    }

    public InsertStatementBuilder addColumnString(String columnName, String value) {
        columnValues.add(FieldType.String, columnName, value);
        return this;
    }

    public InsertStatementBuilder addColumnInteger(String columnName, Integer value) {
        columnValues.add(FieldType.Integer, columnName, value);
        return this;
    }

    public InsertStatementBuilder addColumnTimestamp(String columnName, Date value) {
        columnValues.add(FieldType.Timestamp, columnName, value);
        return this;
    }

    public InsertStatementBuilder addColumnMap(String columnName, Map<String, String> value) {
        columnValues.add(FieldType.Map, columnName, value);
        return this;
    }

    public InsertStatementBuilder addColumnBoolean(String columnName, Boolean value) {
        columnValues.add(FieldType.Boolean, columnName, value);
        return this;
    }

    public BoundStatement build() {

        Insert insert = QueryBuilder.insertInto(super.keyspace, super.table);

        for (FieldAndValue field: columnValues) {
            insert = insert.value(field.getColumnName(), QueryBuilder.bindMarker(field.getColumnName()));
        }

        return super.build(insert, columnValues);
    }
}
