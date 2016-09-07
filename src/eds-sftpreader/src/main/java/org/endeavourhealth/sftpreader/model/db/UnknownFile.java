package org.endeavourhealth.sftpreader.model.db;

import java.time.LocalDateTime;

public class UnknownFile
{
    private int unknownFileId;
    private LocalDateTime insertDate;
    private String filename;
    private LocalDateTime remoteCreatedDate;
    private long remoteSizeBytes;

    public int getUnknownFileId()
    {
        return unknownFileId;
    }

    public UnknownFile setUnknownFileId(int unknownFileId)
    {
        this.unknownFileId = unknownFileId;
        return this;
    }

    public LocalDateTime getInsertDate()
    {
        return insertDate;
    }

    public UnknownFile setInsertDate(LocalDateTime insertDate)
    {
        this.insertDate = insertDate;
        return this;
    }

    public String getFilename()
    {
        return filename;
    }

    public UnknownFile setFilename(String filename)
    {
        this.filename = filename;
        return this;
    }

    public LocalDateTime getRemoteCreatedDate()
    {
        return remoteCreatedDate;
    }

    public UnknownFile setRemoteCreatedDate(LocalDateTime remoteCreatedDate)
    {
        this.remoteCreatedDate = remoteCreatedDate;
        return this;
    }

    public long getRemoteSizeBytes()
    {
        return remoteSizeBytes;
    }

    public UnknownFile setRemoteSizeBytes(long remoteSizeBytes)
    {
        this.remoteSizeBytes = remoteSizeBytes;
        return this;
    }
}
