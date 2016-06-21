package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirUri;

public enum ImmunizationStatus {

    IN_PROGRESS("in-progress", "In Progress"), //The administration has started but has not yet completed
    ON_HOLD("on-hold", "On Hold"), //Actions implied by the administration have been temporarily halted, but are expected to continue later. May also be called "suspended"
    COMPLETED("completed", "Completed"), //All actions that are implied by the administration have occurred.
    ENTERED_IN_ERROR("entered-in-error", "Entered in Error"), //The administration was entered in error and therefore nullified.
    STOPPED("stopped", "Stopped"); //Actions implied by the administration have been permanently halted, before all of them occurred.

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirUri.VALUE_SET_IMMUNIZATION_STATUS;
    }


    ImmunizationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
