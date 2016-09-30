package org.endeavourhealth.transform.ui.models;

import java.util.Date;

public class UICondition {
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

    public void setOnsetDate(Date onsetDate) {
        this.onsetDate = onsetDate;
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

    public void setCode(UICodeableConcept code) {
        this.code = code;
    }
}
