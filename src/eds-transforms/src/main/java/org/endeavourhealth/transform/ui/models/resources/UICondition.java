package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICondition extends UIClinicalResource<UICondition> {
    private UIEncounter encounter;
    private UIPractitioner asserter;
    private UICodeableConcept code;
    private String clinicalStatus;
    private String verificationStatus;
    private Date onsetDate;
    private Date abatementDate;
    private Boolean hasAbated;
    private String notes;
    private UIProblem partOfProblem;

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

    public UICondition setAsserter(UIPractitioner asserter) {
        this.asserter = asserter;
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

    public Date getAbatementDate() {
        return abatementDate;
    }

    public UICondition setAbatementDate(Date abatementDate) {
        this.abatementDate = abatementDate;
        return this;
    }
}
