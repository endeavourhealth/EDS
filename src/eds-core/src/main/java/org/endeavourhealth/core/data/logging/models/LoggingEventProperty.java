package org.endeavourhealth.core.data.logging.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "logging", name = "logging_event_property")
public class LoggingEventProperty {

    @PartitionKey
    @Column(name = "event_id")
    private UUID event_id = null;
    @PartitionKey(value = 1)
    @Column(name = "mapped_key")
    private String mappedKey = null;
    @Column(name = "mapped_value")
    private String mappedValue = null;

    public UUID getEvent_id() {
        return event_id;
    }

    public void setEvent_id(UUID event_id) {
        this.event_id = event_id;
    }

    public String getMappedKey() {
        return mappedKey;
    }

    public void setMappedKey(String mappedKey) {
        this.mappedKey = mappedKey;
    }

    public String getMappedValue() {
        return mappedValue;
    }

    public void setMappedValue(String mappedValue) {
        this.mappedValue = mappedValue;
    }
}
