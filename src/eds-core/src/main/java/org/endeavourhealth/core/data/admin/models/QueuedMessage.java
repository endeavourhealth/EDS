package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "admin", name = "queuedMessage")
public class QueuedMessage {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "messageBody")
    private String messageBody;

    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String name) {
        this.messageBody = name;
    }
}