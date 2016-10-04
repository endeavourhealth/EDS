package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICode {
    private String system;
    private String code;
    private String display;

    public String getSystem() {
        return system;
    }

    public UICode setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getCode() {
        return code;
    }

    public UICode setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDisplay() {
        return display;
    }

    public UICode setDisplay(String display) {
        this.display = display;
        return this;
    }
}
