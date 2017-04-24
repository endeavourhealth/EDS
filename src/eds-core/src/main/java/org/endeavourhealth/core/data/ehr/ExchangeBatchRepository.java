package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.ehr.accessors.ExchangeBatchAccessor;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;

import java.util.List;
import java.util.UUID;

public class ExchangeBatchRepository extends Repository {

    public void save(ExchangeBatch exchangeBatch) {
        if (exchangeBatch == null) {
            throw new IllegalArgumentException("exchangeBatch is null");
        }

        Mapper<ExchangeBatch> mapper = getMappingManager().mapper(ExchangeBatch.class);
        mapper.save(exchangeBatch);
    }

    public List<ExchangeBatch> retrieveForExchangeId(UUID exchangeId) {
        ExchangeBatchAccessor accessor = getMappingManager().createAccessor(ExchangeBatchAccessor.class);
        return Lists.newArrayList(accessor.getForExchangeId(exchangeId));
    }

    public ExchangeBatch retrieveFirstForExchangeId(UUID exchangeId) {
        ExchangeBatchAccessor accessor = getMappingManager().createAccessor(ExchangeBatchAccessor.class);
        return accessor.getFirstForExchangeId(exchangeId);
    }

    public ExchangeBatch getForExchangeAndBatchId(UUID exchangeId, UUID batchId) {
        Mapper<ExchangeBatch> mapper = getMappingManager().mapper(ExchangeBatch.class);
        return mapper.get(exchangeId, batchId);
    }

    //TODO - to be removed
    public ExchangeBatch retrieveFirstForBatchId(UUID batchId) {
        ExchangeBatchAccessor accessor = getMappingManager().createAccessor(ExchangeBatchAccessor.class);
        return accessor.getFirstForBatchId(batchId);
    }
}
