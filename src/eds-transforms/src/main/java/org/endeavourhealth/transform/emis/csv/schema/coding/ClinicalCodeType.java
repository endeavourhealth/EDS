package org.endeavourhealth.transform.emis.csv.schema.coding;

public enum ClinicalCodeType {

    Adminisation_Documents_Attachments("Administration, documents and attachments"),
    Allergy_Adverse_Drug_Reations("Allergy and adverse drug reactions"),
    Allergy_Adverse_Reations("Allergy and adverse reactions"),
    Biochemistry("Biochemistry"),
    Biological_Values("Biological values"),
    Body_Structure("Body structure"),
    Care_Episode_Outcome("Care episode outcome"),
    Conditions_Operations_Procedures("Conditions, operations and procedures"),
    Cyology_Histology("Cytology/Histology"),
    Dental_Disorder("Dental disorder"),
    Dental_Finding("Dental finding"),
    Dental_Procedure("Dental procedure"),
    Diagnostics("Diagnostics"),
    Discharged_From_Service("Discharged from service"),
    EMIS_Qualifier("EMIS Qualifier"),
    Ethnicity("Ethnicity"),
    Family_History("Family history"),
    Haematology("Haematology"),
    Health_Management("Health management, screening and monitoring"),
    HMP("HMP"),
    Immunisations("Immunisations"),
    Immunology("Immunology"),
    Intervention_Category("Intervention category"),
    Intervention_Target("Intervention target"),
    Investigation_Requests("Investigation requests"),
    KC60("KC60"),
    Marital_Status("Marital status"),
    Microbiology("Microbiology"),
    Nationality("Nationality"),
    Nursing_Problem("Nursing problem"),
    Nursing_Problem_Domain("Nursing problem domain"),
    Obsteterics_Birth("Obstetrics and Birth"),
    Pathology_Specimen("Pathology specimen"),
    Person_Health_Social("Personal Health and Social"),
    Planned_Dental("Planned dental intervention"),
    Problem_Rating_Scale("Problem rating scale for outcomes"),
    Procedure("Procedure code"),
    Radiology("Radiology"),
    Reason_For_Care("Reason for care"),
    Referral("Referral"),
    Referral_Activity("Referral activity"),
    Referral_Rejected("Referral rejected"),
    Referral_Withdrawn("Referral withdrawn"),
    Regiment("Regiment"),
    Religion("Religion"),
    Symptoms_Findings("Symptoms and Findings"),
    Trade_Branch("Trade/Branch"),
    Unset("Unset");

    private String value;

    ClinicalCodeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ClinicalCodeType fromValue(String v) {
        for (ClinicalCodeType c: ClinicalCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
