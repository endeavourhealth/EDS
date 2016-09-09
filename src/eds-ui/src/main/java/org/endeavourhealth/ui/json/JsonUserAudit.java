package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.audit.models.UserAudit;
import org.endeavourhealth.core.data.audit.models.UserEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonUserAudit {
    private String pageState = null;
    private List<JsonUserEvent> userEvents = null;

    public JsonUserAudit() {
    }

    public JsonUserAudit(UserAudit userAudit) throws IOException {
        this.pageState = userAudit.getPageState();
        this.userEvents = new ArrayList<>();

        for(UserEvent userEvent : userAudit.getUserEvents()) {
            userEvents.add(new JsonUserEvent(userEvent));
        }
    }

    public String getPageState() {
        return pageState;
    }

    public void setPageState(String pageState) {
        this.pageState = pageState;
    }

    public List<JsonUserEvent> getUserEvents() {
        return userEvents;
    }

    public void setUserEvents(List<JsonUserEvent> userEvents) {
        this.userEvents = userEvents;
    }
}
