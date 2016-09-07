package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.models.UserEvent;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;

import java.io.IOException;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonUserEvent {
    private UUID userId = null;
    private String module = null;
    private String subModule = null;
    private String action = null;
    private UUID serviceId = null;
    private Date timestamp = null;
    private String data = null;


    public JsonUserEvent() {
    }

    public JsonUserEvent(UserEvent userEvent) throws IOException {
        this.userId = userEvent.getUserId();
        this.module = userEvent.getModule();
        this.subModule = userEvent.getSubModule();
        this.action = userEvent.getAction();
        this.serviceId = userEvent.getServiceId();
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

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
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
