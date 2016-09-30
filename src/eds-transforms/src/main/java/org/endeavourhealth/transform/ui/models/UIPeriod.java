package org.endeavourhealth.transform.ui.models;

import java.util.Date;

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
