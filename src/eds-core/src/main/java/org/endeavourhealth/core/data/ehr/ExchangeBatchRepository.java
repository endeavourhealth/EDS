package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;

public class ExchangeBatchRepository extends Repository {

    public void save(ExchangeBatch exchangeBatch) {
        if (exchangeBatch == null) {
            throw new IllegalArgumentException("exchangeBatch is null");
        }

        Mapper<ExchangeBatch> mapper = getMappingManager().mapper(ExchangeBatch.class);
        mapper.save(exchangeBatch);
    }
}
