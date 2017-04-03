package org.endeavourhealth.transform.ui.models.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.helpers.DateHelper;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPeriod {
    private UIDate start;
    private UIDate end;

    public UIDate getStart() {
        return start;
    }

    public UIPeriod setStart(Date start) {
        this.start = DateHelper.convert(start);
        return this;
    }

    public UIDate getEnd() {
        return end;
    }

    public UIPeriod setEnd(Date end) {
        this.end =  DateHelper.convert(end);
        return this;
    }
}
