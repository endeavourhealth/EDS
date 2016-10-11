package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "audit", name = "exchange_transform_by_service_and_system")
public class ExchangeTransformByServiceAndSystem {

    @PartitionKey(0)
    @Column(name = "service_id")
    private UUID serviceId = null;

    @PartitionKey(1)
    @Column(name = "system_id")
    private UUID systemId = null;

    @ClusteringColumn(1)
    @Column(name = "exchange_id")
    private UUID exchangeId = null;

    @ClusteringColumn(0)
    @Column(name = "version")
    private UUID version = null;

    @Column(name = "error_xml")
    private String errorXml = null;



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

    public String getErrorXml() {
        return errorXml;
    }

    public void setErrorXml(String errorXml) {
        this.errorXml = errorXml;
    }
}
