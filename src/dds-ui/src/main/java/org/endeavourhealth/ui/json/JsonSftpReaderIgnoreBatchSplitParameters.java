package org.endeavourhealth.ui.json;

public class JsonSftpReaderIgnoreBatchSplitParameters {
    private String configurationId;
    private int batchId;
    private int batchSplitId;
    private String reason;

    public JsonSftpReaderIgnoreBatchSplitParameters() {
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public int getBatchSplitId() {
        return batchSplitId;
    }

    public void setBatchSplitId(int batchSplitId) {
        this.batchSplitId = batchSplitId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
