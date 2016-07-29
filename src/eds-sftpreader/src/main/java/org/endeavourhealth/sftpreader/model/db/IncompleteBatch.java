package org.endeavourhealth.sftpreader.model.db;

import java.util.ArrayList;
import java.util.List;

public class IncompleteBatch
{
    private int batchId;
    private String batchIdentifier;
    private String localRelativePath;
    private List<IncompleteBatchFile> incompleteBatchFiles = new ArrayList<>();

    public int getBatchId()
    {
        return batchId;
    }

    public IncompleteBatch setBatchId(int batchId)
    {
        this.batchId = batchId;
        return this;
    }

    public String getBatchIdentifier()
    {
        return batchIdentifier;
    }

    public IncompleteBatch setBatchIdentifier(String batchIdentifier)
    {
        this.batchIdentifier = batchIdentifier;
        return this;
    }

    public String getLocalRelativePath()
    {
        return localRelativePath;
    }

    public IncompleteBatch setLocalRelativePath(String localRelativePath)
    {
        this.localRelativePath = localRelativePath;
        return this;
    }

    public IncompleteBatch addIncompleteBatchFile(IncompleteBatchFile incompleteBatchFile)
    {
        this.incompleteBatchFiles.add(incompleteBatchFile);
        return this;
    }
}
