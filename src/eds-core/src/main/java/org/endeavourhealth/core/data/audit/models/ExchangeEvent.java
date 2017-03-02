package org.endeavourhealth.core.data.audit.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "audit", name = "exchange_event")
public class ExchangeEvent {

    @PartitionKey(value = 1)
    @Column(name = "timestamp")
    private Date timestamp = null;

    @PartitionKey(value = 0)
    @Column(name = "exchange_id")
    private UUID exchangeId = null;

    @Column(name = "event_desc")
    private String eventDesc = null;

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

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }
}
