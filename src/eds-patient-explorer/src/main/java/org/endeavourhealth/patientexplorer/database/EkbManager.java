package org.endeavourhealth.patientexplorer.database;



import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class EkbManager {
    public static List<ConceptEntity> search(String term, int maxResultsSize, int start) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select c" +
                    " from " +
                    "    ConceptEntity c" +
                    " where" +
                    "    upper(c.display) like :term " +
                    " order by "+
                    "    length(c.display) ";

        term = term.toUpperCase();

        Query query = entityManager.createQuery(sql, ConceptEntity.class)
                .setParameter("term", "%"+term+"%")
                .setFirstResult(start * maxResultsSize)
                .setMaxResults(maxResultsSize);

        List<ConceptEntity> ret = query.getResultList();

			entityManager.close();

        return ret;
    }

    public static ConceptEntity getConcept(String code) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select c" +
            " from " +
            "    ConceptEntity c" +
            " where" +
            "    c.code = :code ";

        Query query = entityManager.createQuery(sql, ConceptEntity.class)
            .setParameter("code", code);

        List<ConceptEntity> ret = query.getResultList();

        entityManager.close();

        if (ret.size() > 0)
            return ret.get(0);
        else
            return null;
    }

    public static List<ConceptEntity> getChildren(String code) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select r" +
            " from ConceptEntity c" +
            " join ConceptPcLinkEntity l on l.parent_pid = c.pid " +
            " join ConceptEntity r on r.pid = l.child_pid " +
            " where" +
            "    c.code = :code ";

        Query query = entityManager.createQuery(sql, ConceptEntity.class)
            .setParameter("code", code);

        List<ConceptEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }


    public static List<ConceptEntity> getParents(String code) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        String sql = "select r" +
            " from ConceptEntity c" +
            " join ConceptPcLinkEntity l on l.child_pid = c.pid " +
            " join ConceptEntity r on r.pid = l.parent_pid " +
            " where" +
            "    c.code = :code ";

        Query query = entityManager.createQuery(sql, ConceptEntity.class)
            .setParameter("code", code);

        List<ConceptEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
