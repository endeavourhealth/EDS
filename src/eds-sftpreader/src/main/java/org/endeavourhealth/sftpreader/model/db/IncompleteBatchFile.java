package org.endeavourhealth.sftpreader.model.db;

public class IncompleteBatchFile
{
    private int batchId;
    private int batchFileId;
    private String fileTypeIdentifier;
    private String filename;
    private long remoteSizeBytes;
    private boolean isDownloaded;
    private long localSizeBytes;
    private boolean requiresDecryption;
    private boolean isDecrypted;
    private String decryptedFilename;
    private long decryptedSizeBytes;

    public int getBatchId()
    {
        return batchId;
    }

    public IncompleteBatchFile setBatchId(int batchId)
    {
        this.batchId = batchId;
        return this;
    }

    public int getBatchFileId()
    {
        return batchFileId;
    }

    public IncompleteBatchFile setBatchFileId(int batchFileId)
    {
        this.batchFileId = batchFileId;
        return this;
    }

    public String getFileTypeIdentifier()
    {
        return fileTypeIdentifier;
    }

    public IncompleteBatchFile setFileTypeIdentifier(String fileTypeIdentifier)
    {
        this.fileTypeIdentifier = fileTypeIdentifier;
        return this;
    }

    public String getFilename()
    {
        return filename;
    }

    public IncompleteBatchFile setFilename(String filename)
    {
        this.filename = filename;
        return this;
    }

    public long getRemoteSizeBytes()
    {
        return remoteSizeBytes;
    }

    public IncompleteBatchFile setRemoteSizeBytes(long remoteSizeBytes)
    {
        this.remoteSizeBytes = remoteSizeBytes;
        return this;
    }

    public boolean isDownloaded()
    {
        return isDownloaded;
    }

    public IncompleteBatchFile setDownloaded(boolean downloaded)
    {
        isDownloaded = downloaded;
        return this;
    }

    public long getLocalSizeBytes()
    {
        return localSizeBytes;
    }

    public IncompleteBatchFile setLocalSizeBytes(long localSizeBytes)
    {
        this.localSizeBytes = localSizeBytes;
        return this;
    }

    public boolean isRequiresDecryption()
    {
        return requiresDecryption;
    }

    public IncompleteBatchFile setRequiresDecryption(boolean requiresDecryption)
    {
        this.requiresDecryption = requiresDecryption;
        return this;
    }

    public boolean isDecrypted()
    {
        return isDecrypted;
    }

    public IncompleteBatchFile setDecrypted(boolean decrypted)
    {
        isDecrypted = decrypted;
        return this;
    }

    public String getDecryptedFilename()
    {
        return decryptedFilename;
    }

    public IncompleteBatchFile setDecryptedFilename(String decryptedFilename)
    {
        this.decryptedFilename = decryptedFilename;
        return this;
    }

    public long getDecryptedSizeBytes()
    {
        return decryptedSizeBytes;
    }

    public IncompleteBatchFile setDecryptedSizeBytes(long decryptedSizeBytes)
    {
        this.decryptedSizeBytes = decryptedSizeBytes;
        return this;
    }
}
