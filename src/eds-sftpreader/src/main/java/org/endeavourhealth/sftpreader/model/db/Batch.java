package org.endeavourhealth.sftpreader.model.db;

import java.util.ArrayList;
import java.util.List;

public class Batch
{
    private int batchId;
    private String batchIdentifier;
    private String localRelativePath;
    private Integer sequenceNumber;
    private List<BatchFile> batchFiles = new ArrayList<>();

    public int getBatchId()
    {
        return batchId;
    }

    public Batch setBatchId(int batchId)
    {
        this.batchId = batchId;
        return this;
    }

    public String getBatchIdentifier()
    {
        return batchIdentifier;
    }

    public Batch setBatchIdentifier(String batchIdentifier)
    {
        this.batchIdentifier = batchIdentifier;
        return this;
    }

    public String getLocalRelativePath()
    {
        return localRelativePath;
    }

    public Batch setLocalRelativePath(String localRelativePath)
    {
        this.localRelativePath = localRelativePath;
        return this;
    }

    public Integer getSequenceNumber()
    {
        return this.sequenceNumber;
    }

    public Batch setSequenceNumber(Integer sequenceNumber)
    {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public Batch addBatchFile(BatchFile batchFile)
    {
        this.batchFiles.add(batchFile);
        return this;
    }

    public List<BatchFile> getBatchFiles()
    {
        return this.batchFiles;
    }
}
