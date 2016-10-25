package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "audit", name = "service_start")
public class ServiceStart {

    @PartitionKey(0)
    @Column(name = "service_id")
    private UUID serviceId = null;

    @PartitionKey(1)
    @Column(name = "system_id")
    private UUID systemId = null;

    @Column(name = "started")
    private Date started = null;

    public ServiceStart() {}

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }
}
