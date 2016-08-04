package org.endeavourhealth.core.data.audit;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.audit.models.Exchange;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;

public class AuditRepository extends Repository{

    public void save(Exchange exchange, ExchangeEvent event) {

        BatchStatement batch = new BatchStatement();

        //exchange will only be non-null when writing the first event for an exchange
        if (exchange != null) {
            Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
            batch.add(mapperExchange.saveQuery(exchange));
        }

        Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
        batch.add(mapperEvent.saveQuery(event));

        getSession().execute(batch);
    }

}
