package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;

public class Coding_DrugCode extends AbstractCsvTransformer {

    public Coding_DrugCode(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
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
