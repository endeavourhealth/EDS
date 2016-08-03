package org.endeavourhealth.sftpreader.implementations;

import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;

import java.util.List;
import java.util.Map;

public abstract class SftpBatchSequencer
{
    public abstract Map<Batch, Integer> determineBatchSequenceNumbers(List<Batch> incompleteBatches, int nextSequenceNumber, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException;
}
