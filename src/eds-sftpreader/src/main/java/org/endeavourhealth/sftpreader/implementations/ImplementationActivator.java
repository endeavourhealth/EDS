package org.endeavourhealth.sftpreader.implementations;

import org.endeavourhealth.sftpreader.implementations.emis.EmisSftpBatchSequencer;
import org.endeavourhealth.sftpreader.implementations.emis.EmisSftpBatchValidator;
import org.endeavourhealth.sftpreader.implementations.emis.EmisSftpFilenameParser;
import org.endeavourhealth.sftpreader.implementations.emis.EmisSftpNotificationCreator;

import java.util.List;

public class ImplementationActivator
{
    // do this properly - instatiate dynamically based on configuration against interface type

    public static SftpFilenameParser createFilenameParser(String filename, String pgpFileExtensionFilter, List<String> interfaceFileTypes)
    {
        return new EmisSftpFilenameParser(filename, pgpFileExtensionFilter, interfaceFileTypes);
    }

    public static SftpBatchValidator createSftpBatchValidator()
    {
        return new EmisSftpBatchValidator();
    }

    public static SftpBatchSequencer createSftpBatchSequencer()
    {
        return new EmisSftpBatchSequencer();
    }

    public static SftpNotificationCreator createSftpNotificationCreator() { return new EmisSftpNotificationCreator(); }
}
