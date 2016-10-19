package org.endeavourhealth.transform.emis.csv;

import java.io.File;

public class CsvCurrentState {

    private String fileName;
    private Long recordNumber;

    public CsvCurrentState(File file, long recordNumber) {
        this.fileName = file.getName();
        this.recordNumber = new Long(recordNumber);
    }

    public String getFileName() {
        return fileName;
    }

    public Long getRecordNumber() {
        return recordNumber;
    }

    public String toString() {
        if (recordNumber == null) {
            return fileName;
        } else {
            return fileName + " line: " + recordNumber;
        }
    }
}
