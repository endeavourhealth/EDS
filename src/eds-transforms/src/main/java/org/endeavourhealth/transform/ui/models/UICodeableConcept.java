package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICodeableConcept {
    private UICode[] uiCodes = new UICode[0];
    private String text;

    public UICode[] getUiCodes() {
        return uiCodes;
    }

    public UICodeableConcept setUiCodes(List<UICode> uiCodes) {
        this.uiCodes = uiCodes.toArray(new UICode[uiCodes.size()]);
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
