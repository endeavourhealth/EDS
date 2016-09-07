package org.endeavourhealth.transform.emis.csv.schema.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

public class DrugCode extends AbstractCsvTransformer {

    public DrugCode(String version, String folderPath, CSVFormat csvFormat) throws Exception {
        super(version, folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
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
