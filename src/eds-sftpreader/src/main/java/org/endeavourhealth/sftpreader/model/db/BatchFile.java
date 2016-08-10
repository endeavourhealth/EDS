package org.endeavourhealth.sftpreader.model.db;

public class BatchFile
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

    public BatchFile setBatchId(int batchId)
    {
        this.batchId = batchId;
        return this;
    }

    public int getBatchFileId()
    {
        return batchFileId;
    }

    public BatchFile setBatchFileId(int batchFileId)
    {
        this.batchFileId = batchFileId;
        return this;
    }

    public String getFileTypeIdentifier()
    {
        return fileTypeIdentifier;
    }

    public BatchFile setFileTypeIdentifier(String fileTypeIdentifier)
    {
        this.fileTypeIdentifier = fileTypeIdentifier;
        return this;
    }

    public String getFilename()
    {
        return filename;
    }

    public BatchFile setFilename(String filename)
    {
        this.filename = filename;
        return this;
    }

    public long getRemoteSizeBytes()
    {
        return remoteSizeBytes;
    }

    public BatchFile setRemoteSizeBytes(long remoteSizeBytes)
    {
        this.remoteSizeBytes = remoteSizeBytes;
        return this;
    }

    public boolean isDownloaded()
    {
        return isDownloaded;
    }

    public BatchFile setDownloaded(boolean downloaded)
    {
        isDownloaded = downloaded;
        return this;
    }

    public long getLocalSizeBytes()
    {
        return localSizeBytes;
    }

    public BatchFile setLocalSizeBytes(long localSizeBytes)
    {
        this.localSizeBytes = localSizeBytes;
        return this;
    }

    public boolean isRequiresDecryption()
    {
        return requiresDecryption;
    }

    public BatchFile setRequiresDecryption(boolean requiresDecryption)
    {
        this.requiresDecryption = requiresDecryption;
        return this;
    }

    public boolean isDecrypted()
    {
        return isDecrypted;
    }

    public BatchFile setDecrypted(boolean decrypted)
    {
        isDecrypted = decrypted;
        return this;
    }

    public String getDecryptedFilename()
    {
        return decryptedFilename;
    }

    public BatchFile setDecryptedFilename(String decryptedFilename)
    {
        this.decryptedFilename = decryptedFilename;
        return this;
    }

    public long getDecryptedSizeBytes()
    {
        return decryptedSizeBytes;
    }

    public BatchFile setDecryptedSizeBytes(long decryptedSizeBytes)
    {
        this.decryptedSizeBytes = decryptedSizeBytes;
        return this;
    }
}
