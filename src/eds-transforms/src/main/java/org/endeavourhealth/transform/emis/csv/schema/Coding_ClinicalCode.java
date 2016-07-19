package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

public class Coding_ClinicalCode extends AbstractCsvTransformer {

    public Coding_ClinicalCode(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "CodeId",
                "Term",
                "ReadTermId",
                "SnomedCTConceptId",
                "SnomedCTDescriptionId",
                "NationalCode",
                "NationalCodeCategory",
                "NationalDescription",
                "EmisCodeCategoryDescription",
                "ProcessingId"
        };
    }

    public Long getCodeId() {
        return super.getLong("CodeId");
    }
    public String getTerm() {
        return super.getString("Term");
    }
    public String getReadTermId() {
        return super.getString("ReadTermId");
    }
    public Long getSnomedCTConceptId() {
        return super.getLong("SnomedCTConceptId");
    }
    public Long getSnomedCTDescriptionId() {
        return super.getLong("SnomedCTDescriptionId");
    }
    public String getNationalCode() {
        return super.getString("NationalCode");
    }
    public String getNationalCodeCategory() {
        return super.getString("NationalCodeCategory");
    }
    public String getNationalDescription() {
        return super.getString("NationalDescription");
    }
    public String getEmisCodeCategoryDescription() {
        return super.getString("EmisCodeCategoryDescription");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
