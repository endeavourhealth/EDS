package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "audit", name = "exchange_transform_error_to_reprocess")
public class ExchangeTransformErrorToReProcess {

    @PartitionKey(0)
    @Column(name = "service_id")
    private UUID serviceId = null;

    @PartitionKey(1)
    @Column(name = "system_id")
    private UUID systemId = null;

    @ClusteringColumn
    @Column(name = "exchange_id")
    private UUID exchangeId = null;

    public ExchangeTransformErrorToReProcess() {}

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
}
