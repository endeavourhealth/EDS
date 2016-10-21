package org.endeavourhealth.sftpreader.implementations.emis;

import org.endeavourhealth.sftpreader.implementations.SftpBatchSequencer;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;

import java.util.*;
import java.util.stream.Collectors;

public class EmisSftpBatchSequencer extends SftpBatchSequencer
{
    @Override
    public Map<Batch, Integer> determineBatchSequenceNumbers(List<Batch> incompleteBatches, int nextSequenceNumber, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException
    {
        Map<Batch, Integer> result = new HashMap<>();

        List<Batch> orderedIncompleteBatches = incompleteBatches
                .stream()
                .sorted(Comparator.comparing(t -> EmisSftpFilenameParser.parseBatchIdentifier(t.getBatchIdentifier())))
                .collect(Collectors.toList());

        for (Batch batch : orderedIncompleteBatches)
            result.put(batch, nextSequenceNumber++);

        return result;
    }
}
