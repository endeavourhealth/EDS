package org.endeavourhealth.transform.ui.models;

import java.util.Date;

public class UICondition extends UIResource {
    private Date dateRecorded;
    private Date onsetDate;
    private UIPractitioner asserter;
    private UIPractitioner recorder;
    private UICodeableConcept code;

    public Date getDateRecorded() {
        return dateRecorded;
    }

    public void setDateRecorded(Date dateRecorded) {
        this.dateRecorded = dateRecorded;
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
}
