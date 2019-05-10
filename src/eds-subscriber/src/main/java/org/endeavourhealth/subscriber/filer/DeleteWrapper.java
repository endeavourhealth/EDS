package org.endeavourhealth.subscriber.filer;

import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Map;


public class DeleteWrapper {
    private String tableName = null;
    private CSVRecord record = null;
    private List<String> columns = null;
    private Map<String, Class> columnClasses = null;

    public DeleteWrapper(String tableName, CSVRecord record, List<String> columns, Map<String, Class> columnClasses) {
        this.tableName = tableName;
        this.record = record;
        this.columns = columns;
        this.columnClasses = columnClasses;
    }

    public String getTableName() {
        return tableName;
    }

    public CSVRecord getRecord() {
        return record;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Map<String, Class> getColumnClasses() {
        return columnClasses;
    }
}