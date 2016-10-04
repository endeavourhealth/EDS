package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;
import org.hl7.fhir.instance.model.Encounter;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIObservation extends UIClinicalResource<UIObservation> {
    private String status;
    private Date effectiveDateTime;
    private UIPractitioner performer;
    private UIQuantity value;
    private UIQuantity referenceRangeLow;
    private UIQuantity referenceRangeHigh;
    private String comments;
    private Encounter encounter;

    public String getStatus() {
        return status;
    }

    public UIObservation setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public UIObservation setEffectiveDateTime(Date effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
        return this;
    }

    public UIPractitioner getPerformer() {
        return performer;
    }

    public UIObservation setPerformer(UIPractitioner performer) {
        this.performer = performer;
        return this;
    }

    public UIQuantity getValue() {
        return value;
    }

    public UIObservation setValue(UIQuantity value) {
        this.value = value;
        return this;
    }

    public UIQuantity getReferenceRangeLow() {
        return referenceRangeLow;
    }

    public UIObservation setReferenceRangeLow(UIQuantity referenceRangeLow) {
        this.referenceRangeLow = referenceRangeLow;
        return this;
    }

    public UIQuantity getReferenceRangeHigh() {
        return referenceRangeHigh;
    }

    public UIObservation setReferenceRangeHigh(UIQuantity referenceRangeHigh) {
        this.referenceRangeHigh = referenceRangeHigh;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public UIObservation setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public UIObservation setEncounter(Encounter encounter) {
        this.encounter = encounter;
        return this;
    }
}
