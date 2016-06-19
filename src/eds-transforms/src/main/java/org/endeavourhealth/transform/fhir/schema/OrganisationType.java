package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirUri;

public enum OrganisationType {

    APPLICATION_SERVICE_PROVIDER("AR", "Application Service Provider"),
    BOOKING_MANAGEMENT_SYSTEM("BM", "Booking Management System (BMS) Call Centre Establishment"),
    CANCER_NETWORK("CN", "Cancer Network"),
    CANCER_REGISTRY("CR", "Cancer Registry"),
    CARE_HOME_HQ("CQ", "Care Home Headquarters"),
    CARE_TRUST("CT", "Care Trust"),
    CCG("CC", "Clinical Commissioning Group (CCG)"),
    CLINCIAL_NETWORK("CL", "Clinical Network"),
    CSU("CA", "Commissioning Support Unit (CSU)"),
    COURT("JG", "Court"),
    DENTAL_PRACTICE("DD", "Dental Practice"),
    EDUCATION_ESTABLISHMENT("ED", "Education Establishment"),
    EXECUTIVE_AGENCY("EA", "Executive Agency"),
    EXECUTIVE_AGENCY_PROGRAMME("AP", "Executive Agency Programme"),
    GOVERNMENT_DEPARTMENT("GD", "Government Department"),
    GOVERNMENT_OFFICE_REGION("GO", "Government Office Region (GOR)"),
    ABEYANCE_AND_DISPERSALE_GP_PRACTICE("AA", "Abeyance and Dispersal GP Practice"),
    GP_PRACTICE("PR", "GP Practices in England and Wales"),
    HIGH_LEVEL_HEALTH_GEOGRAPHY("HA", "High Level Health Geography"),
    IMMIGRATION_REMOVAL_CENTRE("JD", "Immigration Removal Centre"),
    ISHP("PH", "Independent Sector Healthcare Provider (ISHP)"),
    LOCAL_AUTHORITY("EL", "Local Authority"),
    LOCAL_HEALTH_BOARD_WALES("LB", "Local Health Board (Wales)"),
    LSP("LO", "Local Service Provider (LSP)"),
    MILITARY_HOSPITAL("MH", "Military Hospital"),
    NATIONAL_APPLICATION_SERVICE_PROVIDER("NP", "National Application Service Provider"),
    NATIONAL_GROUPINGS("RO", "National Groupings"),
    NHS_SUPPORT_AGENCY("NS", "NHS Support Agency"),
    NHS_TRUST("TR", "NHS Trust"),
    NON_NHS_ORGANISATION("NN", "Non-NHS ORGANISATION"),
    NI_HEALTH_AND_SOCIAL_CARE_BOARD("NA", "Northern Ireland Health & Social Care Board"),
    NI_HEALTH_AND_SOCIAL_CARE_TRUST("NB", "Northern Ireland Health & Social Care Trust"),
    NI_LOCAL_COMMISSIONING_GROUP("NC", "Northern Ireland Local Commissioning Group"),
    OPTICAL_HQ("OH", "Optical Headquarters"),
    OSX("OA", "Other Statutory Authority (OSA)"),
    PHARMACY("PY", "Pharmacy"),
    PHARMACY_HQ("PX", "Pharmacy Headquarters"),
    POLICE_CONSTABULARY("JE", "Police Constabulary"),
    POLICE_CUSTODY_SUITE("JF", "Police Custody Suite"),
    PCT("PT", "Primary Care Trust"),
    PRIMARY_HEALTHCARE_DIRECTORATE("ID", "Primary Healthcare Directorate (Isle of Man)"),
    PRISON_HEALTH_SERVICE("PN", "Prison Health Service"),
    SCHOOL("EE", "School"),
    SECURE_CHILDRENS_HOME("JC", "Secure Children's Home"),
    SECTURE_TRAINING_CENTRE("JB", "Secure Training Centre (STC)"),
    SEXUAL_ASSUALT_REFERRAL_CENTRE("JH", "Sexual Assault Referral Centre (SARC)"),
    SPECIAL_HEALTH_AUTHORITY("SA", "Special Health Authority (SpHA)"),
    WELSH_ASSEMBLY("WA", "Welsh Assembly"),
    WELSH_HEALTH_COMMISSION("WH", "Welsh Health Commission"),
    WELSH_LOCAL_HEALTH_BOARD("LH", "Welsh Local Health Board"),
    YOUNG_OFFENDERS_INSTITUTE("JA", "Young Offenders Institute");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSystem() {
        return FhirUri.VALUE_SET_ORGANISATION_TYPE;
    }

    OrganisationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrganisationType fromDescription(String v) {
        for (OrganisationType c: OrganisationType.values()) {
            if (c.description.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
