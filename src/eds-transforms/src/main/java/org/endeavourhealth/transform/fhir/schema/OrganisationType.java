package org.endeavourhealth.transform.fhir.schema;

public enum OrganisationType {

    AR("Application Service Provider"),
    BM("Booking Management System (BMS) Call Centre Establishment"),
    CN("Cancer Network"),
    CR("Cancer Registry"),
    CQ("Care Home Headquarters"),
    CT("Care Trust"),
    CC("Clinical Commissioning Group (CCG)"),
    CL("Clinical Network"),
    CA("Commissioning Support Unit (CSU)"),
    JG("Court"),
    DD("Dental Practice"),
    ED("Education Establishment"),
    EA("Executive Agency"),
    AP("Executive Agency Programme"),
    GD("Government Department"),
    GO("Government Office Region (GOR)"),
    AA("Abeyance and Dispersal GP Practice"),
    PR("GP Practices in England and Wales"),
    HA("High Level Health Geography"),
    JD("Immigration Removal Centre"),
    PH("Independent Sector Healthcare Provider (ISHP)"),
    EL("Local Authority"),
    LB("Local Health Board (Wales)"),
    LO("Local Service Provider (LSP)"),
    MH("Military Hospital"),
    NP("National Application Service Provider"),
    RO("National Groupings"),
    NS("NHS Support Agency"),
    TR("NHS Trust"),
    NN("Non-NHS ORGANISATION"),
    NA("Northern Ireland Health & Social Care Board"),
    NB("Northern Ireland Health & Social Care Trust"),
    NC("Northern Ireland Local Commissioning Group"),
    OH("Optical Headquarters"),
    OA("Other Statutory Authority (OSA)"),
    PY("Pharmacy"),
    PX("Pharmacy Headquarters"),
    JE("Police Constabulary"),
    JF("Police Custody Suite"),
    PT("Primary Care Trust"),
    ID("Primary Healthcare Directorate (Isle of Man)"),
    PN("Prison Health Service"),
    EE("School"),
    JC("Secure Children's Home"),
    JB("Secure Training Centre (STC)"),
    JH("Sexual Assault Referral Centre (SARC)"),
    SA("Special Health Authority (SpHA)"),
    WA("Welsh Assembly"),
    WH("Welsh Health Commission"),
    LH("Welsh Local Health Board"),
    JA("Young Offenders Institute");

    private String value = null;

    public String getValue() {
        return value;
    }

    OrganisationType(String value) {
        this.value = value;
    }


    public static OrganisationType fromValue(String v) {
        for (OrganisationType c: OrganisationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
