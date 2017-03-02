package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.audit.models.UserEvent;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonUserEvent {
    private UUID userId = null;
    private String module = null;
    private String subModule = null;
    private String action = null;
    private UUID organisationId = null;
    private Date timestamp = null;
    private String data = null;


    public JsonUserEvent() {
    }

    public JsonUserEvent(UserEvent userEvent) {
        this.userId = userEvent.getUserId();
        this.module = userEvent.getModule();
        this.subModule = userEvent.getSubModule();
        this.action = userEvent.getAction();
        this.organisationId = userEvent.getOrganisationId();
        this.timestamp = userEvent.getTimestamp();
        this.data = userEvent.getData();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getSubModule() {
        return subModule;
    }

    public void setSubModule(String subModule) {
        this.subModule = subModule;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * gets/sets
     */
}
