package org.endeavourhealth.sftpreader.batchFileImplementations;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;

import java.time.LocalDateTime;

public abstract class BatchFile
{
    private SftpRemoteFile sftpRemoteFile;
    private String localRootPath;
    protected String pgpFileExtensionFilter;

    BatchFile(SftpRemoteFile sftpRemoteFile, String localRootPath, String pgpFileExtensionFilter)
    {
        this.sftpRemoteFile = sftpRemoteFile;
        this.localRootPath = localRootPath;
        this.pgpFileExtensionFilter = pgpFileExtensionFilter;
    }

    public abstract boolean isFilenameValid();
    public abstract String getRemoteBatchIdentifier();
    public abstract String getRemoteFileTypeIdentifier();

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
        return FilenameUtils.concat(this.localRootPath, this.getRemoteBatchIdentifier());
    }

    public String getLocalRelativePath()
    {
        return this.getRemoteBatchIdentifier();
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
        return StringUtils.removeEnd(getLocalFilePath(), this.pgpFileExtensionFilter);
    }

    public long getRemoteFileSizeInBytes()
    {
        return sftpRemoteFile.getFileSizeBytes();
    }

    public LocalDateTime getRemoteLastModifiedDate()
    {
        return sftpRemoteFile.getLastModified();
    }
}
