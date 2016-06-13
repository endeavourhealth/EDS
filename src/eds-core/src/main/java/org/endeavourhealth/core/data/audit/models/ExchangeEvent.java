package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "audit", name = "exchangeEvent")
public class ExchangeEvent {

    @PartitionKey(value = 1)
    @Column(name = "timestamp")
    private Date timestamp = null;
    @PartitionKey(value = 0)
    @Column(name = "exchangeId")
    private UUID exchangeId = null;
    @Column(name = "event")
    private Integer event = null;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

    public Integer getEvent() {
        return event;
    }

    public void setEvent(Integer event) {
        this.event = event;
    }
}
