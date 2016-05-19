package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;

public class Coding_DrugCode extends AbstractCsvTransformer {
    public Coding_DrugCode(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public Long getCodeId() {
        return super.getLong(0);
    }
    public String getTerm() {
        return super.getString(1);
    }
    public Long getDmdProductCodeId() {
        return super.getLong(2);
    }
    public Integer getProcessigId() {
        return super.getInt(3);
    }
}
