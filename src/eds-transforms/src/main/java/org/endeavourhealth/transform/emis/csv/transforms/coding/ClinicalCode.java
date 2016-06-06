package org.endeavourhealth.transform.emis.csv.transforms.coding;

/**
 * temporary storage object used during parsing the EMIS CSV extract
 */
public class ClinicalCode {

    private String emisTerm = null;
    private String emisCode = null;
    private String emisCategory = null;

    private String nationalCodeCategory = null;
    private String nationalCode = null;
    private String nationalDescription = null;

    private long snomedConceptId = -1;
    private long snomedDescriptionId = -1;

    public ClinicalCode(String emisTerm, String emisCode, String emisCategory,
                        String nationalCodeCategory, String nationalCode, String nationalDescription,
                        long snomedConceptId, long snomedDescriptionId) {

        this.emisTerm = emisTerm;
        this.emisCode = emisCode;
        this.emisCategory = emisCategory;
        this.nationalCodeCategory = nationalCodeCategory;
        this.nationalCode = nationalCode;
        this.nationalDescription = nationalDescription;
        this.snomedConceptId = snomedConceptId;
        this.snomedDescriptionId = snomedDescriptionId;
    }

    public String getEmisTerm() {
        return emisTerm;
    }

    public String getEmisCode() {
        return emisCode;
    }

    public String getEmisCategory() {
        return emisCategory;
    }

    public String getNationalCodeCategory() {
        return nationalCodeCategory;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public String getNationalDescription() {
        return nationalDescription;
    }

    public long getSnomedConceptId() {
        return snomedConceptId;
    }

    public long getSnomedDescriptionId() {
        return snomedDescriptionId;
    }
}