package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPractitioner {
    private String displayName;
    private Boolean active;

    public String getDisplayName() {
        return displayName;
    }

    public UIPractitioner setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public UIPractitioner setActive(Boolean active) {
        this.active = active;
        return this;
    }
}
