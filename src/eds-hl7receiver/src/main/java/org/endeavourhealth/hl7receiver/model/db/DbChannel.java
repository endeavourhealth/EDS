package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbChannel {
    private int channelId;
    private String channelName;
    private int portNumber;
    private boolean useTls;
    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private List<DbChannelMessageType> dbChannelMessageTypes;

    public String getSendingApplication() {
        return sendingApplication;
    }

    public DbChannel setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
        return this;
    }

    public String getSendingFacility() {
        return sendingFacility;
    }

    public DbChannel setSendingFacility(String sendingFacility) {
        this.sendingFacility = sendingFacility;
        return this;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public DbChannel setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
        return this;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public DbChannel setReceivingFacility(String receivingFacility) {
        this.receivingFacility = receivingFacility;
        return this;
    }

    private String notes;

    public int getChannelId() {
        return channelId;
    }

    public DbChannel setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getChannelName() {
        return channelName;
    }

    public DbChannel setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public DbChannel setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public DbChannel setUseTls(boolean useTls) {
        this.useTls = useTls;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public DbChannel setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public List<DbChannelMessageType> getDbChannelMessageTypes() {
        return dbChannelMessageTypes;
    }

    public DbChannel setDbChannelMessageTypes(List<DbChannelMessageType> dbChannelMessageTypes) {
        this.dbChannelMessageTypes = dbChannelMessageTypes;
        return this;
    }
}
