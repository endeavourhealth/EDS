package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPeriod {
    private Date start;
    private Date end;

    public Date getStart() {
        return start;
    }

    public UIPeriod setStart(Date start) {
        this.start = start;
        return this;
    }

    public Date getEnd() {
        return end;
    }

    public UIPeriod setEnd(Date end) {
        this.end = end;
        return this;
    }
}
