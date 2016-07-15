package org.endeavourhealth.sftpreader.utilities.sftp;

public class SftpRemoteFile
{
    public SftpRemoteFile(String filename)
    {
        this.filename = filename;
    }

    private String filename;

    public String getFilename()
    {
        return filename;
    }
}
