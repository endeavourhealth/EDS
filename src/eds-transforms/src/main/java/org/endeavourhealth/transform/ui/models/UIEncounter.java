package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIEncounter extends UIResource {
    private String status;
    private UIPractitioner performedBy;
    private UIPractitioner enteredBy;
    private List<UICode> reason = new ArrayList<>();
    private UIPeriod period;

    public String getStatus() {
        return status;
    }

    public UIEncounter setStatus(String status) {
        this.status = status;
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

    public UIPeriod getPeriod() {
        return period;
    }

    public UIEncounter setPeriod(UIPeriod period) {
        this.period = period;
        return this;
    }
}
