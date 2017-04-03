package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICodeableConcept {
    private UICode[] codes = new UICode[0];
    private String text;

    public UICode[] getCodes() {
        return codes;
    }

    public UICodeableConcept setCodes(List<UICode> codes) {
        this.codes = codes.toArray(new UICode[codes.size()]);
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
