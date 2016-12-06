package org.endeavourhealth.hl7receiver.model.db;

public class DbChannel {
    private int channelId;
    private String channelName;
    private int portNumber;
    private boolean useTls;
    private String remoteApplication;
    private String remoteFacility;
    private String localApplication;
    private String localFacility;
    private boolean useAcks;
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

    public String getRemoteApplication() {
        return remoteApplication;
    }

    public DbChannel setRemoteApplication(String remoteApplication) {
        this.remoteApplication = remoteApplication;
        return this;
    }

    public String getRemoteFacility() {
        return remoteFacility;
    }

    public DbChannel setRemoteFacility(String remoteFacility) {
        this.remoteFacility = remoteFacility;
        return this;
    }

    public String getLocalApplication() {
        return localApplication;
    }

    public DbChannel setLocalApplication(String localApplication) {
        this.localApplication = localApplication;
        return this;
    }

    public String getLocalFacility() {
        return localFacility;
    }

    public DbChannel setLocalFacility(String localFacility) {
        this.localFacility = localFacility;
        return this;
    }

    public boolean isUseAcks() {
        return useAcks;
    }

    public DbChannel setUseAcks(boolean useAcks) {
        this.useAcks = useAcks;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public DbChannel setNotes(String notes) {
        this.notes = notes;
        return this;
    }
}
