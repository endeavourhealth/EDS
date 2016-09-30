package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICodeableConcept {
    private List<UICode> uiCodes = new ArrayList<>();
    private String text;

    public List<UICode> getUiCodes() {
        return uiCodes;
    }

    public UICodeableConcept setUiCodes(List<UICode> uiCodes) {
        this.uiCodes = uiCodes;
        return this;
    }

    public String getText() {
        return text;
    }

    public UICodeableConcept setText(String text) {
        this.text = text;
        return this;
    }
}
