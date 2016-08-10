package org.endeavourhealth.ui.database;


import org.endeavourhealth.ui.database.models.LoggingEventEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

/**
 * Created by darren on 21/07/16.
 */
public class DataManager {

    public static List<LoggingEventEntity> getLoggingEvents(String serviceId) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select e" +
                    " from " +
                    "    LoggingEventEntity e," +
                    "    LoggingEventPropertyEntity p" +
                    " where" +
                    "    e.eventId = p.eventId" +
                    "    and p.mappedKey = 'ServiceId'" +
                    "    and p.mappedValue = :serviceId";

        List<LoggingEventEntity> events = entityManager.createQuery(sql, LoggingEventEntity.class)
                .setParameter("serviceId", serviceId)
                .getResultList();

        entityManager.close();

        return events;
    }



}
