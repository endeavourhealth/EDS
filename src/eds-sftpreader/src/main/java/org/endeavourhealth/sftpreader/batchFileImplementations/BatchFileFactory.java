package org.endeavourhealth.sftpreader.batchFileImplementations;

import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;

import java.util.List;

public class BatchFileFactory
{
    private BatchFileFactory()
    {
    }

    public static BatchFile create(SftpRemoteFile sftpRemoteFile, String localRootPath, String pgpFileExtensionFilter, List<String> validFileTypeIdentifiers)
    {
        //TODO create batch file based on batch file type or configuration

        return new EmisBatchFile(sftpRemoteFile, localRootPath, pgpFileExtensionFilter, validFileTypeIdentifiers);
    }
}
