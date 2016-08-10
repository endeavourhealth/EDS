package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum EncounterParticipantType {

    //defined at http://hl7.org/fhir/valueset-encounter-participant-type.html
    TRANSLATOR("translator", "Translator"),
    EMERGENCY("emergency", "Emergency"),
    ADMITTER("ADM", "admitter"),
    ATTENDER("ATND", "attender"),
    CALLBACK("CALLBCK", "callback contact"),
    CONSULTANT("CON", "consultant"),
    DISCHARGER("DIS", "discharger"),
    ESCORT("ESC", "escort"),
    REFERRER("REF", "referrer"),
    SECONDARY_PERFORMER("SPRF", "secondary performer"),
    PRIMARY_PERFORMER("PPRF", "primary performer"),
    PARTICIPANT("PART", "Participation");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_ENCOUNTER_PARTICIPANT_TYPE;
    }

    EncounterParticipantType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static EncounterParticipantType fromCode(String v) {
        for (EncounterParticipantType c: EncounterParticipantType.values()) {
            if (c.code.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
