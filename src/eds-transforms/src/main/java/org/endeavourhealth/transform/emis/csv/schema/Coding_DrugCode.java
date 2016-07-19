package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

public class Coding_DrugCode extends AbstractCsvTransformer {

    public Coding_DrugCode(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
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
