package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;

import java.util.UUID;

@Accessor
public interface ExchangeBatchAccessor {

    @Query("SELECT * FROM ehr.exchange_batch WHERE exchange_id = :exchange_id")
    Result<ExchangeBatch> getForExchangeId(@Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM ehr.exchange_batch WHERE exchange_id = :exchange_id LIMIT 1")
    ExchangeBatch getFirstForExchangeId(@Param("exchange_id") UUID exchangeId);

    @Query("SELECT * FROM ehr.exchange_batch WHERE batch_id = :batch_id LIMIT 1 ALLOW FILTERING")
    ExchangeBatch getFirstForBatchId(@Param("batch_id") UUID batchId);

}