package org.endeavourhealth.transform.ui.models;

import java.util.Date;

public class UICondition extends UIResource {
    private Date dateRecorded;
    private Date onsetDate;
    private Date endDate;
    private Boolean hasEnded;
    private UIPractitioner asserter;
    private UIPractitioner recorder;
    private UICodeableConcept code;
    private String notes;

    public Date getDateRecorded() {
        return dateRecorded;
    }

    public UICondition setDateRecorded(Date dateRecorded) {
        this.dateRecorded = dateRecorded;
        return this;
    }

    public Date getOnsetDate() {
        return onsetDate;
    }

    public UICondition setOnsetDate(Date onsetDate) {
        this.onsetDate = onsetDate;
        return this;
    }

    public UIPractitioner getAsserter() {
        return asserter;
    }

    public void setAsserter(UIPractitioner asserter) {
        this.asserter = asserter;
    }

    public UIPractitioner getRecorder() {
        return recorder;
    }

    public void setRecorder(UIPractitioner recorder) {
        this.recorder = recorder;
    }

    public UICodeableConcept getCode() {
        return code;
    }

    public UICondition setCode(UICodeableConcept code) {
        this.code = code;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public UICondition setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Boolean getHasEnded() {
        return hasEnded;
    }

    public UICondition setHasEnded(Boolean hasEnded) {
        this.hasEnded = hasEnded;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public UICondition setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }
}
