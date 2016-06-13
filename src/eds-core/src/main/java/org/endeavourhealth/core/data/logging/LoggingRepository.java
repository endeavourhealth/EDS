package org.endeavourhealth.core.data.logging;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.logging.models.LoggingEvent;
import org.endeavourhealth.core.data.logging.models.LoggingEventException;
import org.endeavourhealth.core.data.logging.models.LoggingEventProperty;

import java.util.List;

public class LoggingRepository extends Repository {

    public void save(LoggingEvent event, List<LoggingEventProperty> properties, List<LoggingEventException> exceptionLines){

        BatchStatement batch = new BatchStatement();

        Mapper<LoggingEvent> mapperEvent = getMappingManager().mapper(LoggingEvent.class);
        batch.add(mapperEvent.saveQuery(event));

        if (properties != null && !properties.isEmpty()) {
            Mapper<LoggingEventProperty> mapperProperty = getMappingManager().mapper(LoggingEventProperty.class);

            for (LoggingEventProperty loggingEventProperty: properties) {
                batch.add(mapperProperty.saveQuery(loggingEventProperty));
            }
        }

        if (exceptionLines != null && !exceptionLines.isEmpty()) {
            Mapper<LoggingEventException> mapperException = getMappingManager().mapper(LoggingEventException.class);

            for (LoggingEventException exceptionLine: exceptionLines) {
                batch.add(mapperException.saveQuery(exceptionLine));
            }
        }

        getSession().execute(batch);
    }
}
