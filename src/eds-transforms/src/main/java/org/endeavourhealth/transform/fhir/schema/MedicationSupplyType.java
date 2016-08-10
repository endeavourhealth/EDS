package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum MedicationSupplyType {

    NHS_PRESCRIPTION("394823007", "NHS Prescription"),
    PRIVATE_PRESCRIPTION("394824001", "Private Prescription"),
    ACBS_PRESCRIPTION("394825000", "ACBS Prescription"),
    OTC_SALE("394826004", "OTC sale"),
    PERSONNALLY_ADMINISTERED("394827008", "Personal Administration"),
    PRESCRIPTION_BY_OTHER_ORGANISATION("394828003", "Prescription by another organisation"),
    PAST_MEDICATION("394829006", "Past medication");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_MEDICATION_SUPPLY_TYPE;
    }

    MedicationSupplyType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
