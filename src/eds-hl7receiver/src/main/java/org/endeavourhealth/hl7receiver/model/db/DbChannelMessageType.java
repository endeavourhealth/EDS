package org.endeavourhealth.hl7receiver.model.db;

public class DbChannelMessageType {
    private int channelId;
    private String messageType;
    private boolean isActive;

    public int getChannelId() {
        return channelId;
    }

    public DbChannelMessageType setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getMessageType() {
        return messageType;
    }

    public DbChannelMessageType setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public DbChannelMessageType setActive(boolean active) {
        isActive = active;
        return this;
    }
}
