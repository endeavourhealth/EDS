package org.endeavourhealth.ui.querydocument.models;

/**
 * Created by darren on 19/05/16.
 */
public class Protocol {

    protected Boolean enabled;
    protected Boolean patientConsent;

    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    public Boolean getPatientConsent() {
        return patientConsent;
    }
    public void setPatientConsent(Boolean value) {
        this.patientConsent = value;
    }
}
