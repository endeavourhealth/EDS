package org.endeavourhealth.transform.emis.csv.schema.coding;

import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;

public class DrugCode extends AbstractCsvParser {

    public DrugCode(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "CodeId",
                "Term",
                "DmdProductCodeId",
                "ProcessingId"
        };
    }

    public Long getCodeId() {
        return super.getLong("CodeId");
    }
    public String getTerm() {
        return super.getString("Term");
    }
    public Long getDmdProductCodeId() {
        return super.getLong("DmdProductCodeId");
    }
    public Integer getProcessigId() {
        return super.getInt("ProcessingId");
    }
}
