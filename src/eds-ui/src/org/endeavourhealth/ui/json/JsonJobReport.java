package org.endeavourhealth.ui.json;

import java.util.Date;

public final class JsonJobReport {
    private String name = null;
    private Date date = null;

    public JsonJobReport() {}
    public JsonJobReport(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
