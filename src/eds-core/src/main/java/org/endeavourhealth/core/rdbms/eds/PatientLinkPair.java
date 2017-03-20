package org.endeavourhealth.core.rdbms.eds;

public class PatientLinkPair {

    private String patientId = null;
    private String newPersonId = null;
    private String previousPersonId = null;

    public PatientLinkPair(String patientId, String newPersonId, String previousPersonId) {
        this.patientId = patientId;
        this.newPersonId = newPersonId;
        this.previousPersonId = previousPersonId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getNewPersonId() {
        return newPersonId;
    }

    public void setNewPersonId(String newPersonId) {
        this.newPersonId = newPersonId;
    }

    public String getPreviousPersonId() {
        return previousPersonId;
    }

    public void setPreviousPersonId(String previousPersonId) {
        this.previousPersonId = previousPersonId;
    }

    public String toString() {
        return "PatientId " + patientId + " NewPersonId " + newPersonId + " PreviousPersonId " + previousPersonId;
    }

}
