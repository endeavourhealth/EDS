package org.endeavourhealth.sftpreader.utilities.sftp;

import org.apache.commons.io.FilenameUtils;

import java.time.LocalDateTime;

public class SftpRemoteFile
{
    private String filename;
    private String remotePath;
    private long fileSizeBytes;
    private LocalDateTime lastModified;

    public SftpRemoteFile(String filename, String remotePath, long fileSizeBytes, LocalDateTime lastModified)
    {
        this.filename = filename;
        this.remotePath = remotePath;
        this.fileSizeBytes = fileSizeBytes;
        this.lastModified = lastModified;
    }

    public String getFullPath()
    {
        return FilenameUtils.concat(remotePath, filename);
    }

    public String getFilename()
    {
        return filename;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public long getFileSizeBytes()
    {
        return fileSizeBytes;
    }

    public LocalDateTime getLastModified()
    {
        return lastModified;
    }
}
