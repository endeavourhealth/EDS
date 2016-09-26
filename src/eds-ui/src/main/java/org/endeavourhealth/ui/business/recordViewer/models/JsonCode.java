package org.endeavourhealth.ui.business.recordViewer.models;

public class JsonCode {
    private String system;
    private String code;
    private String display;

    public String getSystem() {
        return system;
    }

    public JsonCode setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getCode() {
        return code;
    }

    public JsonCode setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDisplay() {
        return display;
    }

    public JsonCode setDisplay(String display) {
        this.display = display;
        return this;
    }
}
