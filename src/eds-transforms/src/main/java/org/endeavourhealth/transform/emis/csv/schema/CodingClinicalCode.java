package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;

public class CodingClinicalCode extends AbstractCsvTransformer {
    public CodingClinicalCode(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public Long getCodeId() {
        return super.getLong(0);
    }
    public String getCodeTerm() {
        return super.getString(1);
    }
    public String getReadTermId() {
        return super.getString(2);
    }
    public String getNationalCodeCategory() {
        return super.getString(3);
    }
    public String getNationalCode() {
        return super.getString(4);
    }
    public String getNationalDescription() {
        return super.getString(5);
    }
    public Long getSnomedCTConceptId() {
        return super.getLong(6);
    }
    public Long getSnomedCTDescriptionId() {
        return super.getLong(7);
    }
    public String getEmisCodeCategoryDescription() {
        return super.getString(8);
    }
    public Integer getProcessingId() {
        return super.getInt(9);
    }
}
