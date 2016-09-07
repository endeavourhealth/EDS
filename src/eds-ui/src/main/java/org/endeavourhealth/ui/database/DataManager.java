package org.endeavourhealth.ui.database;


import org.endeavourhealth.ui.database.models.LoggingEventEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by darren on 21/07/16.
 */
public class DataManager {
    private static final int PAGESIZE = 15;
    public static List<LoggingEventEntity> getLoggingEvents(int page, String serviceId, String level) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select e" +
                    " from " +
                    "    LoggingEventEntity e," +
                    "    LoggingEventPropertyEntity p" +
                    " where" +
                    "    e.eventId = p.eventId" +
                    "    and p.mappedKey = 'ServiceId'" +
                    "    and p.mappedValue = :serviceId";

        if (level != null && !level.isEmpty())
            sql += "    and e.levelString = :level";

        Query query = entityManager.createQuery(sql, LoggingEventEntity.class)
                .setParameter("serviceId", serviceId)
                .setFirstResult(page * PAGESIZE)
                .setMaxResults(PAGESIZE);

        if (level != null && !level.isEmpty())
            query.setParameter("level", level);

        List<LoggingEventEntity> events = query.getResultList();

        entityManager.close();

        return events;
    }

    public static String getStackTrace(Long eventId) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select e.traceLine" +
            " from " +
            "    LoggingEventExceptionEntity e" +
            " where" +
            "    e.eventId = :eventId";

        Query query = entityManager.createQuery(sql, String.class)
            .setParameter("eventId", eventId);

        List<String> stackTrace = query.getResultList();

        entityManager.close();

        return String.join(System.lineSeparator(), stackTrace);
    }
}
