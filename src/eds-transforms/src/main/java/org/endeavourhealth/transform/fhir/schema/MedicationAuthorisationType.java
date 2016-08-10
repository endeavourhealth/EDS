package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum MedicationAuthorisationType {

    //defined at http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-type-extension
    ACUTE("acute", "Acute"),
    REPEAT("repeat", "Repeat"),
    REPEAT_DISPENSING("repeatDispensing", "Repeat Dispensing"),
    AUTOMATIC("automatic", "Automatic");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_MEDICATION_AUTHORISATION_TYPE;
    }

    MedicationAuthorisationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MedicationAuthorisationType fromDescription(String v) {
        for (MedicationAuthorisationType c: MedicationAuthorisationType.values()) {
            if (c.getDescription().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
