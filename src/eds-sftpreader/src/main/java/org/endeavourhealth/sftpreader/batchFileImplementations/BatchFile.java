package org.endeavourhealth.sftpreader.batchFileImplementations;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;

import java.time.LocalDateTime;
import java.util.List;

public abstract class BatchFile
{
    private SftpRemoteFile sftpRemoteFile;
    private String localRootPath;
    protected String pgpFileExtensionFilter;
    protected List<String> validFileTypeIdentifiers;
    private Long localFileSizeBytes = null;
    private Long decryptedFileSizeBytes = null;
    private Integer batchFileId = null;

    BatchFile(SftpRemoteFile sftpRemoteFile, String localRootPath, String pgpFileExtensionFilter, List<String> validFileTypeIdentifiers)
    {
        this.sftpRemoteFile = sftpRemoteFile;
        this.localRootPath = localRootPath;
        this.pgpFileExtensionFilter = pgpFileExtensionFilter;
        this.validFileTypeIdentifiers = validFileTypeIdentifiers;
    }

    public abstract boolean isFilenameValid();
    public abstract String getBatchIdentifier();
    public abstract String getFileTypeIdentifier();

    public String getRemoteFilePath()
    {
        return this.sftpRemoteFile.getFullPath();
    }

    public String getFilename()
    {
        return this.sftpRemoteFile.getFilename();
    }

    public String getLocalPath()
    {
        return FilenameUtils.concat(this.localRootPath, this.getBatchIdentifier());
    }

    public String getLocalRelativePath()
    {
        return this.getBatchIdentifier();
    }

    public String getLocalFilePath()
    {
        return FilenameUtils.concat(getLocalPath(), getFilename());
    }

    public boolean doesFileNeedDecrypting()
    {
        if (StringUtils.isEmpty(pgpFileExtensionFilter))
            return false;

        return (getFilename().endsWith(pgpFileExtensionFilter));
    }

    public String getDecryptedLocalFilePath()
    {
        return FilenameUtils.concat(getLocalPath(), getDecryptedFilename());
    }

    public String getDecryptedFilename()
    {
        return StringUtils.removeEnd(getFilename(), this.pgpFileExtensionFilter);
    }

    public long getRemoteFileSizeInBytes()
    {
        return sftpRemoteFile.getFileSizeBytes();
    }

    public LocalDateTime getRemoteLastModifiedDate()
    {
        return sftpRemoteFile.getLastModified();
    }

    public long getLocalFileSizeBytes()
    {
        if (localFileSizeBytes == null)
            throw new NullPointerException("localFileSizeBytes is null");

        return this.localFileSizeBytes;
    }

    public void setLocalFileSizeBytes(long localFileSizeBytes)
    {
        this.localFileSizeBytes = localFileSizeBytes;
    }

    public long getDecryptedFileSizeBytes()
    {
        if (decryptedFileSizeBytes == null)
            throw new NullPointerException("decryptedFileSizeBytes is null");

        return this.decryptedFileSizeBytes;
    }

    public void setDecryptedFileSizeBytes(long localFileSizeBytes)
    {
        this.decryptedFileSizeBytes = localFileSizeBytes;
    }

    public int getBatchFileId()
    {
        if (batchFileId == null)
            throw new NullPointerException("batchFileId is null");

        return this.batchFileId;
    }

    public void setBatchFileId(int batchFileId)
    {
        this.batchFileId = batchFileId;
    }
}
