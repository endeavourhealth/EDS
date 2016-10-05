package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class UIClinicalResource<T extends UIClinicalResource> extends UIResource<T> {
    private UICodeableConcept code;
    private UIPractitioner effectivePractitioner;
    private Date effectiveDate;
    private UIPractitioner recordingPractitioner;
    private Date recordedDate;
    private String notes;

    public UICodeableConcept getCode() {
        return code;
    }

    public T setCode(UICodeableConcept code) {
        this.code = code;
        return (T)this;
    }

    public UIPractitioner getEffectivePractitioner() {
        return effectivePractitioner;
    }

    public T setEffectivePractitioner(UIPractitioner effectivePractitioner) {
        this.effectivePractitioner = effectivePractitioner;
        return (T)this;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public T setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
        return (T)this;
    }

    public UIPractitioner getRecordingPractitioner() {
        return recordingPractitioner;
    }

    public T setRecordingPractitioner(UIPractitioner recordingPractitioner) {
        this.recordingPractitioner = recordingPractitioner;
        return (T)this;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public T setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
        return (T)this;
    }

    public String getNotes() {
        return notes;
    }

    public T setNotes(String notes) {
        this.notes = notes;
        return (T)this;
    }
}
