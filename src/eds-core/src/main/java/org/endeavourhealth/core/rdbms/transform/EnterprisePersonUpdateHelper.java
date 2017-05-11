package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;

public class EnterprisePersonUpdateHelper {

    public static Date findDatePersonUpdaterLastRun(String enterpriseConfigName) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        String sql = "select c"
                + " from"
                + " EnterprisePersonUpdateHistory c"
                + " order by dateRun desc";

        Query query = entityManager.createQuery(sql, EnterprisePersonUpdateHistory.class);
        query.setMaxResults(1);

        try {
            EnterprisePersonUpdateHistory result = (EnterprisePersonUpdateHistory)query.getSingleResult();
            return result.getDateRun();

        } catch (NoResultException ex) {
            return new Date(0);

        } finally {
            entityManager.close();
        }
    }

    public static void updatePersonUpdaterLastRun(String enterpriseConfigName, Date d) throws Exception {

        EnterprisePersonUpdateHistory history = new EnterprisePersonUpdateHistory();
        history.setDateRun(d);

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(history);
            entityManager.getTransaction().commit();

        } finally {
            entityManager.close();
        }
    }
}
