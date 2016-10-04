package org.endeavourhealth.transform.ui.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class UIClinicalResource<T extends UIClinicalResource> extends UIResource<T> {
    private UIPractitioner recordedBy;
    private Date recordedDate;

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
}
