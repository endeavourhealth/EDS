package org.endeavourhealth.sftpreader.batchFileImplementations;

import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;

public class BatchFileFactory
{
    private BatchFileFactory()
    {
    }

    public static BatchFile create(SftpRemoteFile sftpRemoteFile, String localRootPath, String pgpFileExtensionFilter)
    {
        //TODO create batch file based on batch file type or configuration

        return new EmisBatchFile(sftpRemoteFile, localRootPath, pgpFileExtensionFilter);
    }
}
