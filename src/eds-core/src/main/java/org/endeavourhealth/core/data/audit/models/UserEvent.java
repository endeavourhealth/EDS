package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "audit", name = "user_event")
public class UserEvent {

    @PartitionKey
    @Column(name = "user_id")
    private UUID userId = null;
    @Column(name = "module")
    private String module = null;
    @Column(name = "submodule")
    private String subModule = null;
    @Column(name = "action")
    private String action = null;
    @Column(name = "service_id")
    private UUID serviceId = null;
    @Column(name = "timestamp")
    private Date timestamp = null;
    @Column(name = "data")
    private String data = null;

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UserEvent() {}

    public UserEvent(Row row) {
        this.userId = row.get("user_id", UUID.class);
        this.serviceId = row.get("service_id", UUID.class);
        this.module = row.get("module", String.class);
        this.subModule = row.get("submodule", String.class);
        this.action = row.get("action", String.class);
        this.timestamp = row.get("timestamp", Date.class);
        this.data = row.get("data", String.class);
    }

    public UserEvent(UUID userId, IAuditModule auditModule, IAuditModule auditSubModule, String action, UUID serviceId, String data) {
        this.userId = userId;
        this.serviceId = serviceId;
        this.module = ((Enum)auditModule).name();
        this.subModule = ((Enum)auditSubModule).name();
        this.action = action;
        this.timestamp = new Date();
        this.data = data;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
