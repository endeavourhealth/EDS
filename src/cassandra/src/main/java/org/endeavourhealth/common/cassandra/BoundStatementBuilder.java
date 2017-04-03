package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

abstract class BoundStatementBuilder {

    protected enum FieldType {
        String,
        UUID,
        Integer,
        Timestamp,
        Map,
        Boolean
    }

    protected static class FieldAndValue {
        private final FieldType fieldType;
        private final String columnName;
        private final Object value;

        public FieldAndValue(FieldType fieldType, String columnName, Object value) {
            this.fieldType = fieldType;
            this.columnName = columnName;
            this.value = value;
        }

        public FieldType getFieldType() {
            return fieldType;
        }

        public String getColumnName() {
            return columnName;
        }

        public Object getValue() {
            return value;
        }
    }

    protected static class FieldAndValueList extends ArrayList<FieldAndValue> {
        public void add(FieldType fieldType, String columnName, Object value) {
            super.add(new FieldAndValue(fieldType, columnName, value));
        }
    }

    private final PreparedStatementCache statementCache;
    protected final String keyspace;
    protected final String table;

    BoundStatementBuilder(PreparedStatementCache statementCache, String keyspace, String table) {
        this.statementCache = statementCache;
        this.keyspace = keyspace;
        this.table = table;
    }

    protected BoundStatement build(RegularStatement regularStatement, FieldAndValueList parameters) {

        PreparedStatement preparedStatement = statementCache.getOrAdd(regularStatement);

        BoundStatement boundStatement = preparedStatement.bind();

        for (FieldAndValue field: parameters) {
            boundStatement = setValue(boundStatement, field);
        }

        return boundStatement;
    }

    private BoundStatement setValue(BoundStatement statement, FieldAndValue value) {
        switch (value.getFieldType()) {
            case String:
                return statement.setString(value.getColumnName(), (String)value.getValue());
            case UUID:
                return statement.setUUID(value.getColumnName(), (UUID)value.getValue());
            case Integer:
                return statement.setInt(value.getColumnName(), (Integer)value.getValue());
            case Timestamp:
                return statement.setTimestamp(value.getColumnName(), (Date)value.getValue());
            case Map:
                return statement.setMap(value.getColumnName(), (Map)value.getValue());
            case Boolean:
                return statement.setBool(value.getColumnName(), (Boolean)value.getValue());
            default:
                throw new UnsupportedOperationException("FieldType not supported: " + value.fieldType.toString());
        }
    }
}