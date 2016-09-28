package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIEncounter {
    private String status;
    private UIPractitioner performedBy;
    private UIPractitioner enteredBy;
    private String date;
    private List<UICode> reason = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public UIEncounter setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDate() {
        return date;
    }

    public UIEncounter setDate(String date) {
        this.date = date;
        return this;
    }

    public List<UICode> getReason() {
        return reason;
    }

    public UIEncounter setReason(List<UICode> reason) {
        this.reason = reason;
        return this;
    }

    public UIPractitioner getPerformedBy() {
        return performedBy;
    }

    public UIEncounter setPerformedBy(UIPractitioner performedBy) {
        this.performedBy = performedBy;
        return this;
    }

    public UIPractitioner getEnteredBy() {
        return enteredBy;
    }

    public UIEncounter setEnteredBy(UIPractitioner enteredBy) {
        this.enteredBy = enteredBy;
        return this;
    }
}
