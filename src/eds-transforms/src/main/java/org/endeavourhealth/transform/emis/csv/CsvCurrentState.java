package org.endeavourhealth.transform.emis.csv;

import java.io.File;

public class CsvCurrentState {

    private String filePath;
    private Long recordNumber;

    public CsvCurrentState(File file) {
        this.filePath = file.getAbsolutePath();
    }

    public CsvCurrentState(File file, long recordNumber) {
        this.filePath = file.getAbsolutePath();
        this.recordNumber = new Long(recordNumber);
    }

    public String getFilePath() {
        return filePath;
    }

    public Long getRecordNumber() {
        return recordNumber;
    }

    public String toString() {
        if (recordNumber == null) {
            return filePath;
        } else {
            return filePath + " line: " + recordNumber;
        }
    }
}
