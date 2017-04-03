package org.endeavourhealth.transform.ui.models.types;

import java.util.Date;

public class UIDate {
    private Date date;
    private String precision; // year, month, day, minute, second, millisecond

    public Date getDate() {
        return date;
    }

    public UIDate setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getPrecision() {
        return precision;
    }

    public UIDate setPrecision(String precision) {
        this.precision = precision;
        return this;
    }
}
