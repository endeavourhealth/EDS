package org.endeavourhealth.core.data.logging.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "logging", name = "logging_event_exception")
public class LoggingEventException {

    @PartitionKey
    @Column(name = "event_id")
    private UUID eventId = null;
    @PartitionKey(value = 1)
    @Column(name = "i")
    private Integer lineNumber = null;
    @Column(name = "trace_line")
    private String traceLine = null;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getTraceLine() {
        return traceLine;
    }

    public void setTraceLine(String traceLine) {
        this.traceLine = traceLine;
    }
}
