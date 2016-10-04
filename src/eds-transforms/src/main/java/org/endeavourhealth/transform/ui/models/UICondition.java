package org.endeavourhealth.transform.ui.models;

import java.util.Date;

public class UICondition extends UIResource {
    private UIEncounter encounter;
    private UIPractitioner asserter;
    private Date dateRecorded;
    private UICodeableConcept code;
    private String clinicalStatus;
    private String verificationStatus;
    private Date onset;
    private Date abatement;
    private Boolean hasAbated;
    private String notes;
    private UIProblem partOfProblem;
    private UIPractitioner recorder;

    public UIEncounter getEncounter() {
        return encounter;
    }

    public UICondition setEncounter(UIEncounter encounter) {
        this.encounter = encounter;
        return this;
    }

    public String getClinicalStatus() {
        return clinicalStatus;
    }

    public UICondition setClinicalStatus(String clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
        return this;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public UICondition setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
        return this;
    }

    public UIProblem getPartOfProblem() {
        return partOfProblem;
    }

    public UICondition setPartOfProblem(UIProblem partOfProblem) {
        this.partOfProblem = partOfProblem;
        return this;
    }

    public Date getDateRecorded() {
        return dateRecorded;
    }

    public UICondition setDateRecorded(Date dateRecorded) {
        this.dateRecorded = dateRecorded;
        return this;
    }

    public Date getOnset() {
        return onset;
    }

    public UICondition setOnset(Date onset) {
        this.onset = onset;
        return this;
    }

    public UIPractitioner getAsserter() {
        return asserter;
    }

    public UICondition setAsserter(UIPractitioner asserter) {
        this.asserter = asserter;
        return this;
    }

    public UIPractitioner getRecorder() {
        return recorder;
    }

    public UICondition setRecorder(UIPractitioner recorder) {
        this.recorder = recorder;
        return this;
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

    public Boolean getHasAbated() {
        return hasAbated;
    }

    public UICondition setHasAbated(Boolean hasAbated) {
        this.hasAbated = hasAbated;
        return this;
    }

    public Date getAbatement() {
        return abatement;
    }

    public UICondition setAbatement(Date abatement) {
        this.abatement = abatement;
        return this;
    }
}
