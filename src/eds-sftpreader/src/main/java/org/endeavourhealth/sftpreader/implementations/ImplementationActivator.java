package org.endeavourhealth.sftpreader.implementations;

import org.endeavourhealth.sftpreader.implementations.emis.*;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;

public class ImplementationActivator
{
    // do this properly - instatiate dynamically based on configuration against interface type

    public static SftpFilenameParser createFilenameParser(String filename, DbConfiguration dbConfiguration)
    {
        return new EmisSftpFilenameParser(filename, dbConfiguration);
    }

    public static SftpBatchValidator createSftpBatchValidator()
    {
        return new EmisSftpBatchValidator();
    }

    public static SftpBatchSequencer createSftpBatchSequencer()
    {
        return new EmisSftpBatchSequencer();
    }

    public static SftpNotificationCreator createSftpNotificationCreator() {
        return new EmisSftpNotificationCreator();
    }

    public static SftpBatchSplitter createSftpBatchSplitter() {
        return new EmisSftpBatchSplitter();
    }
}
