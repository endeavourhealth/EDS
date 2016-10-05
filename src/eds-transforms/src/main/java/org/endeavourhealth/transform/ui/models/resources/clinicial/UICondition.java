package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UICondition extends UIClinicalResource<UICondition> {
    private UIEncounter encounter;
    private String clinicalStatus;
    private String verificationStatus;
    private Date abatementDate;
    private Boolean hasAbated;
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
