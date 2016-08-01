package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.lang3.Validate;
import org.endeavourhealth.sftpreader.implementations.SftpBatchSequencer;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmisSftpBatchSequencer extends SftpBatchSequencer
{
    @Override
    public Map<Batch, Integer> determineBatchSequenceNumbers(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException
    {
        Map<Batch, Integer> result = new HashMap<>();

        if (incompleteBatches.size() == 0)
            return result;

        Integer nextSequenceNumber = 1;
        Integer nextProcessingIdStart = null;

        // if there was a previous complete batch
        if (lastCompleteBatch != null)
        {
            nextSequenceNumber = getNextSequenceNumber(lastCompleteBatch);
            nextProcessingIdStart = getNextProcessingIdStart(lastCompleteBatch);
        }
        else
        {
            // find first and assign sequence number
            Batch batch = findBatchWithLowestProcessingIdStart(incompleteBatches);

            result.put(batch, nextSequenceNumber++);
            nextProcessingIdStart = getNextProcessingIdStart(batch);
        }

        // loop until all inputs are in the output
        while (true)
        {
            Batch nextBatch = findBatchWithProcessingIdStart(nextProcessingIdStart, incompleteBatches);

            if (nextBatch == null)
            {
                // either all items have been sequenced
                if (batchArraysEqual(incompleteBatches, new ArrayList<>(result.keySet())))
                    break;

                // or can't find the next item
                throw new SftpValidationException("Cannot find next batch with processing id start of " + nextProcessingIdStart.toString());
            }

            if (result.containsKey(nextBatch))
                throw new SftpValidationException("batch already in result");  // something has gone wrong

            // add to reult
            result.put(nextBatch, nextSequenceNumber++);
            nextProcessingIdStart = getNextProcessingIdStart(nextBatch);
        }

        return result;
    }

    private static Integer getNextSequenceNumber(Batch lastCompleteBatch) throws SftpValidationException
    {
        if (lastCompleteBatch.getSequenceNumber() == null)
            throw new SftpValidationException("lastCompleteBatch.sequenceNumber is null");

        return lastCompleteBatch.getSequenceNumber() + 1;
    }

    private static Integer getNextProcessingIdStart(Batch lastCompleteBatch) throws SftpFilenameParseException
    {
        return ProcessingIdSet.parseBatchIdentifier(lastCompleteBatch.getBatchIdentifier()).getProcessingIdEnd() + 1;
    }

    private boolean batchArraysEqual(List<Batch> batches1, List<Batch> batches2)
    {
        Validate.notNull(batches1, "batches1 is null");
        Validate.notNull(batches2, "batches2 is null");

        for (Batch batch1 : batches1)
            if (!batches2.contains(batch1))
                return false;

        for (Batch batch2 : batches2)
            if (!batches1.contains(batch2))
                return false;

        return true;
    }

    private Batch findBatchWithProcessingIdStart(Integer processingIdStart, List<Batch> incompleteBatches) throws SftpFilenameParseException
    {
        Validate.notNull(processingIdStart, "processingIdStart is null");

        for (Batch incompleteBatch : incompleteBatches)
        {
            ProcessingIdSet processingIdSet = ProcessingIdSet.parseBatchIdentifier(incompleteBatch.getBatchIdentifier());

            if (processingIdSet.getProcessingIdStart() == processingIdStart.intValue())
                return incompleteBatch;
        }

        return null;
    }

    private Batch findBatchWithLowestProcessingIdStart(List<Batch> incompleteBatches) throws SftpFilenameParseException
    {
        Batch lowest = null;
        Integer lowestProcessingIdStart = null;

        for (Batch incompleteBatch : incompleteBatches)
        {
            ProcessingIdSet current = ProcessingIdSet.parseBatchIdentifier(incompleteBatch.getBatchIdentifier());

            if ((lowest == null) || (current.getProcessingIdStart() < lowestProcessingIdStart.intValue()))
            {
                lowest = incompleteBatch;
                lowestProcessingIdStart = current.getProcessingIdStart();
            }
        }

        return lowest;
    }
}
