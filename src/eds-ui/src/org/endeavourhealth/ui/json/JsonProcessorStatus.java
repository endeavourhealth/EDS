package org.endeavourhealth.ui.json;

public final class JsonProcessorStatus {
    private String status = null;

    public JsonProcessorStatus() {}
    public JsonProcessorStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
