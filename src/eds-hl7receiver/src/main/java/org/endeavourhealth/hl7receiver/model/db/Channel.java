package org.endeavourhealth.hl7receiver.model.db;

public class Channel {
    private int channelId;
    private String channelName;
    private String remoteApplication;
    private String remoteFacility;
    private String localApplication;
    private String localFacility;
    private int portNumber;
    private boolean useAcks;
    private String notes;

    public int getChannelId() {
        return channelId;
    }

    public Channel setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getChannelName() {
        return channelName;
    }

    public Channel setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public String getRemoteApplication() {
        return remoteApplication;
    }

    public Channel setRemoteApplication(String remoteApplication) {
        this.remoteApplication = remoteApplication;
        return this;
    }

    public String getRemoteFacility() {
        return remoteFacility;
    }

    public Channel setRemoteFacility(String remoteFacility) {
        this.remoteFacility = remoteFacility;
        return this;
    }

    public String getLocalApplication() {
        return localApplication;
    }

    public Channel setLocalApplication(String localApplication) {
        this.localApplication = localApplication;
        return this;
    }

    public String getLocalFacility() {
        return localFacility;
    }

    public Channel setLocalFacility(String localFacility) {
        this.localFacility = localFacility;
        return this;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public Channel setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public boolean isUseAcks() {
        return useAcks;
    }

    public Channel setUseAcks(boolean useAcks) {
        this.useAcks = useAcks;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public Channel setNotes(String notes) {
        this.notes = notes;
        return this;
    }
}
