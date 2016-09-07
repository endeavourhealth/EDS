package org.endeavourhealth.transform.emis.csv.schema.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

public class ClinicalCode extends AbstractCsvTransformer {

    public ClinicalCode(String version, String folderPath, CSVFormat csvFormat) throws Exception {
        super(version, folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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
