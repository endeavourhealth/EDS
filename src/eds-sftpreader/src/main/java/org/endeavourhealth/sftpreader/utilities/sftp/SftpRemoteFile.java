package org.endeavourhealth.sftpreader.utilities.sftp;

import org.apache.commons.io.FilenameUtils;

public class SftpRemoteFile
{
    private String filename;
    private String remotePath;

    public SftpRemoteFile(String filename, String remotePath)
    {
        this.filename = filename;
        this.remotePath = remotePath;
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
}
