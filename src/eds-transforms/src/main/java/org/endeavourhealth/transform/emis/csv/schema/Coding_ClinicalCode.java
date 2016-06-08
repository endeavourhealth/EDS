package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;

public class Coding_ClinicalCode extends AbstractCsvTransformer {

    public Coding_ClinicalCode(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "CodeId",
                "Term",
                "ReadTermId",
                "NationalCodeCategory",
                "NationalCode",
                "NationalDescription",
                "SnomedCTConceptId",
                "SnomedCTDescriptionId",
                "EmisCodeCategoryDescription",
                "ProcessingId"
        };
    }

    public Long getCodeId() {
        return super.getLong(0);
    }
    public String getTerm() {
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
