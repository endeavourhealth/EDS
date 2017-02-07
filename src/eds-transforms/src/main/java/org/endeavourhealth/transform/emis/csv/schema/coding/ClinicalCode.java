package org.endeavourhealth.transform.emis.csv.schema.coding;

import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;

public class ClinicalCode extends AbstractCsvParser {

    public ClinicalCode(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {

        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_4)) {
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
                    "ProcessingId",
                    "ParentCodeId"
            };

        } else {

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
    public Long getParentCodeId() {
        return super.getLong("ParentCodeId");
    }
}
