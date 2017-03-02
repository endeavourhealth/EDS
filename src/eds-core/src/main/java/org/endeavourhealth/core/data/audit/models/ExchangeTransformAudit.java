package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "audit", name = "exchange_transform_audit")
public class ExchangeTransformAudit {

    @PartitionKey(0)
    @Column(name = "service_id")
    private UUID serviceId = null;

    @PartitionKey(1)
    @Column(name = "system_id")
    private UUID systemId = null;

    @ClusteringColumn(0)
    @Column(name = "exchange_id")
    private UUID exchangeId = null;

    @ClusteringColumn(1)
    @Column(name = "version")
    private UUID version = null;

    @Column(name = "started")
    private Date started = null;

    @Column(name = "ended")
    private Date ended = null;

    @Column(name = "error_xml")
    private String errorXml = null;

    @Column(name = "resubmitted")
    private boolean resubmitted = false;

    @Column(name = "deleted")
    private Date deleted = null;

    @Column(name = "number_batches_created")
    private Integer numberBatchesCreated = null;

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

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date ended) {
        this.ended = ended;
    }

    public String getErrorXml() {
        return errorXml;
    }

    public void setErrorXml(String errorXml) {
        this.errorXml = errorXml;
    }

    public boolean isResubmitted() {
        return resubmitted;
    }

    public void setResubmitted(boolean resubmitted) {
        this.resubmitted = resubmitted;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Integer getNumberBatchesCreated() {
        return numberBatchesCreated;
    }

    public void setNumberBatchesCreated(Integer numberBatchesCreated) {
        this.numberBatchesCreated = numberBatchesCreated;
    }
}
