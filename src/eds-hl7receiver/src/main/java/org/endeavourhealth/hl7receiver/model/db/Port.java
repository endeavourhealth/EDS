package org.endeavourhealth.hl7receiver.model.db;

public class Port {
    private int portNumber;
    private boolean useTls;
    private String notes;

    public int getPortNumber() {
        return portNumber;
    }

    public Port setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public Port setUseTls(boolean useTls) {
        this.useTls = useTls;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public Port setNotes(String notes) {
        this.notes = notes;
        return this;
    }
}
