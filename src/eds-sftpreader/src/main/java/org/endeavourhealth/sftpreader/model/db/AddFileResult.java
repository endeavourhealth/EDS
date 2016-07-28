package org.endeavourhealth.sftpreader.model.db;

public class AddFileResult
{
    private boolean fileAlreadyProcessed;
    private int batchFileId;

    public boolean isFileAlreadyProcessed()
    {
        return fileAlreadyProcessed;
    }

    public AddFileResult setFileAlreadyProcessed(boolean fileAlreadyProcessed)
    {
        this.fileAlreadyProcessed = fileAlreadyProcessed;
        return this;
    }

    public int getBatchFileId()
    {
        return batchFileId;
    }

    public AddFileResult setBatchFileId(int batchFileId)
    {
        this.batchFileId = batchFileId;
        return this;
    }
}
