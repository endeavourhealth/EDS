package org.endeavourhealth.hl7receiver.model.db;

import java.time.LocalDateTime;

public class DbMessage {
    private int messageId;
    private String messageControlId;
    private String messageSequenceNumber;
    private LocalDateTime messageDate;
    private String inboundMessageType;
    private String inboundPayload;
    private DbNotificationStatus notificationStatusId;

    public int getMessageId() {
        return messageId;
    }

    public DbMessage setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getMessageControlId() {
        return messageControlId;
    }

    public DbMessage setMessageControlId(String messageControlId) {
        this.messageControlId = messageControlId;
        return this;
    }

    public String getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public DbMessage setMessageSequenceNumber(String messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
        return this;
    }

    public LocalDateTime getMessageDate() {
        return messageDate;
    }

    public DbMessage setMessageDate(LocalDateTime messageDate) {
        this.messageDate = messageDate;
        return this;
    }

    public String getInboundMessageType() {
        return inboundMessageType;
    }

    public DbMessage setInboundMessageType(String inboundMessageType) {
        this.inboundMessageType = inboundMessageType;
        return this;
    }

    public String getInboundPayload() {
        return inboundPayload;
    }

    public DbMessage setInboundPayload(String inboundPayload) {
        this.inboundPayload = inboundPayload;
        return this;
    }

    public DbNotificationStatus getNotificationStatusId() {
        return notificationStatusId;
    }

    public DbMessage setNotificationStatusId(DbNotificationStatus notificationStatusId) {
        this.notificationStatusId = notificationStatusId;
        return this;
    }
}
