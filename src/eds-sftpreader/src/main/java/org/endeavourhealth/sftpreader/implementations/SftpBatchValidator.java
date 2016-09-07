package org.endeavourhealth.sftpreader.implementations;

import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;

import java.util.List;

public abstract class SftpBatchValidator
{
    public abstract void validateBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch, DbConfiguration dbConfiguration) throws SftpValidationException;
}
