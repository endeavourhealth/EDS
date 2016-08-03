package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.sftpreader.implementations.SftpNotificationCreator;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.db.BatchFile;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmisSftpNotificationCreator extends SftpNotificationCreator
{
    @Override
    public String createNotificationMessage(DbConfiguration dbConfiguration, Batch batch)
    {
        String path = FilenameUtils.concat(dbConfiguration.getLocalRootPath(), batch.getLocalRelativePath());

        List<BatchFile> batchFiles = batch
                .getBatchFiles()
                .stream()
                .sorted(Comparator.comparing(t -> t.getFilename()))
                .collect(Collectors.toList());

        String message = "";

        for (BatchFile batchFile : batchFiles)
            message += FilenameUtils.concat(path, batchFile.getDecryptedFilename()) + System.lineSeparator();

        return message;
    }
}
