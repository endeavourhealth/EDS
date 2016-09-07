package org.endeavourhealth.sftpreader.model.db;

public class BatchSplit {

    private int batchSplitId;
    private int batchId;
    private String localRelativePath;
    private String organisationId;
    private Batch batch;

    public BatchSplit() {}

    public int getBatchSplitId() {
        return batchSplitId;
    }

    public BatchSplit setBatchSplitId(int batchSplitId) {
        this.batchSplitId = batchSplitId;
        return this;
    }

    public int getBatchId() {
        return batchId;
    }

    public BatchSplit setBatchId(int batchId) {
        this.batchId = batchId;
        return this;
    }

    public String getLocalRelativePath() {
        return localRelativePath;
    }

    public BatchSplit setLocalRelativePath(String localRelativePath) {
        this.localRelativePath = localRelativePath;
        return this;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public BatchSplit setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
        return this;
    }

    public Batch getBatch() {
        return batch;
    }

    public BatchSplit setBatch(Batch batch) {
        this.batch = batch;
        return this;
    }
}
