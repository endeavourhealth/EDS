package org.endeavourhealth.ui.business.recordViewer.models;

import java.util.Date;

public class JsonPeriod {
    private Date start;
    private Date end;

    public Date getStart() {
        return start;
    }

    public JsonPeriod setStart(Date start) {
        this.start = start;
        return this;
    }

    public Date getEnd() {
        return end;
    }

    public JsonPeriod setEnd(Date end) {
        this.end = end;
        return this;
    }
}
