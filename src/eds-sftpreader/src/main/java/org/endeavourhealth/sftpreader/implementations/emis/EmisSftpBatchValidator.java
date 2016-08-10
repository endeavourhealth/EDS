package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.sftpreader.implementations.SftpBatchValidator;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.db.BatchFile;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;

import java.time.LocalDateTime;
import java.util.*;

public class EmisSftpBatchValidator extends SftpBatchValidator
{
    @Override
    public void validateBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch, DbConfiguration dbConfiguration) throws SftpValidationException
    {
        Validate.notNull(incompleteBatches, "incompleteBatches is null");
        Validate.notNull(dbConfiguration, "dbConfiguration is null");
        Validate.notNull(dbConfiguration.getInterfaceFileTypes(), "dbConfiguration.interfaceFileTypes is null");
        Validate.notEmpty(dbConfiguration.getInterfaceFileTypes(), "No interface file types configured");

        for (Batch incompleteBatch : incompleteBatches)
        {
            checkFilenamesAreConsistentAcrossBatch(incompleteBatch, dbConfiguration);
            checkAllFilesArePresentInBatch(incompleteBatch, dbConfiguration);

            // further checks to complete
            //
            // check that remote bytes == downloaded bytes
            // check all file attributes are complete
        }
    }

    private void checkFilenamesAreConsistentAcrossBatch(Batch incompleteBatches, DbConfiguration dbConfiguration) throws SftpValidationException
    {
        Validate.notNull(incompleteBatches, "incompleteBatches is null");
        Validate.notNull(incompleteBatches.getBatchFiles(), "incompleteBatches.batchFiles is null");

        Integer processingIdStart = null;
        Integer processingIdEnd = null;
        LocalDateTime extractDateTime = null;

        if (incompleteBatches.getBatchFiles().size() == 0)
            throw new SftpValidationException("No batch files in batch");

        boolean first = true;

        for (BatchFile incompleteBatchFile : incompleteBatches.getBatchFiles())
        {
            EmisSftpFilenameParser emisSftpFilenameParser = new EmisSftpFilenameParser(incompleteBatchFile.getFilename(), dbConfiguration);

            if (first)
            {
                processingIdStart = emisSftpFilenameParser.getProcessingIds().getProcessingIdStart();
                processingIdEnd = emisSftpFilenameParser.getProcessingIds().getProcessingIdEnd();
                extractDateTime = emisSftpFilenameParser.getExtractDateTime();

                first = false;
            }
            else
            {
                if (emisSftpFilenameParser.getProcessingIds().getProcessingIdStart() != processingIdStart)
                    throw new SftpValidationException("Emis start processing id does not match the rest in the batch.  Filename = " + incompleteBatchFile.getFilename());

                if (emisSftpFilenameParser.getProcessingIds().getProcessingIdEnd() != processingIdEnd)
                    throw new SftpValidationException("Emis end processing id does not match the rest in the batch.  Filename = " + incompleteBatchFile.getFilename());

                if (!emisSftpFilenameParser.getExtractDateTime().equals(extractDateTime))
                    throw new SftpValidationException("Emis extract date time does not match the rest in the batch.  Filename = " + incompleteBatchFile.getFilename());
            }
        }
    }

    private void checkAllFilesArePresentInBatch(Batch incompleteBatch, DbConfiguration dbConfiguration) throws SftpValidationException
    {
        for (String fileType : dbConfiguration.getInterfaceFileTypes())
        {
            boolean found = false;

            for (BatchFile incompleteBatchFile : incompleteBatch.getBatchFiles())
                if (fileType.equals(incompleteBatchFile.getFileTypeIdentifier()))
                    found = true;

            if (!found)
                throw new SftpValidationException("Could not find file of type " + fileType + " in batch.  Batch identifier = " + incompleteBatch.getBatchIdentifier());
        }

        if (incompleteBatch.getBatchFiles().size() != dbConfiguration.getInterfaceFileTypes().size())
            throw new SftpValidationException("Incorrect number of files in batch. Batch identifier = " + incompleteBatch.getBatchIdentifier());
    }
}
