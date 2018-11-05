package org.endeavourhealthX.subscriber.filer;

import org.apache.commons.csv.CSVRecord;

import java.util.HashMap;
import java.util.List;


public class DeleteWrapper {
    private String tableName = null;
    private CSVRecord record = null;
    private List<String> columns = null;
    private HashMap<String, Class> columnClasses = null;

    public DeleteWrapper(String tableName, CSVRecord record, List<String> columns, HashMap<String, Class> columnClasses) {
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

    public HashMap<String, Class> getColumnClasses() {
        return columnClasses;
    }
}