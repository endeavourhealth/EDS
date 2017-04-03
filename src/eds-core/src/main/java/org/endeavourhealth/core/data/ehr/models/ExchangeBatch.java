package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "exchange_batch")
public class ExchangeBatch {
    @PartitionKey
    @Column(name = "exchange_id")
    private UUID exchangeId;

    @ClusteringColumn
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "inserted_at")
    private Date insertedAt;

    @Column(name = "eds_patient_id")
    private UUID edsPatientId;

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public Date getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(Date insertedAt) {
        this.insertedAt = insertedAt;
    }

    public UUID getEdsPatientId() {
        return edsPatientId;
    }

    public void setEdsPatientId(UUID edsPatientId) {
        this.edsPatientId = edsPatientId;
    }
}
