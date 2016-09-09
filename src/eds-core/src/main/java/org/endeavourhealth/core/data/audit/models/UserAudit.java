package org.endeavourhealth.core.data.audit.models;

import java.util.List;

public class UserAudit {
    private String pageState = null;
    private List<UserEvent> userEvents = null;

    public UserAudit() {}

    public UserAudit(String pageState, List<UserEvent> userEvents) {
        this.pageState = pageState;
        this.userEvents = userEvents;
    }

    public String getPageState() {
        return pageState;
    }

    public void setPageState(String pageState) {
        this.pageState = pageState;
    }

    public List<UserEvent> getUserEvents() {
        return userEvents;
    }

    public void setUserEvents(List<UserEvent> userEvents) {
        this.userEvents = userEvents;
    }
}
