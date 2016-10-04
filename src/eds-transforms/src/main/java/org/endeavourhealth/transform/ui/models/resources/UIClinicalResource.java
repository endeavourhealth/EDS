package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class UIClinicalResource<T extends UIClinicalResource> extends UIResource<T> {
    private UIPractitioner recordedBy;
    private Date recordedDate;
    private UICodeableConcept code;

    public UIPractitioner getRecordedBy() {
        return recordedBy;
    }

    public T setRecordedBy(UIPractitioner recordedBy) {
        this.recordedBy = recordedBy;
        return (T)this;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public T setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
        return (T)this;
    }

    public UICodeableConcept getCode() {
        return code;
    }

    public T setCode(UICodeableConcept code) {
        this.code = code;
        return (T)this;
    }
}
