package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class PseudoIdHelper {

    public static void storePseudoId(String patientId, String configName, String pseudoId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        PseudoIdMap map = findIdMap(patientId, configName, entityManager);
        if (map == null) {
            map = new PseudoIdMap();
            map.setPatientId(patientId);
            map.setEnterpriseConfigName(configName);
        }
        map.setPseudoId(pseudoId);

        entityManager.getTransaction().begin();
        entityManager.persist(map);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static PseudoIdMap findIdMap(String patientId, String configName, EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " PseudoIdMap c"
                + " where c.patientId = :patientId"
                + " and c.enterpriseConfigName = :enterpriseConfigName";


        Query query = entityManager.createQuery(sql, PseudoIdMap.class)
                .setParameter("patientId", patientId)
                .setParameter("enterpriseConfigName", configName);

        try {
            return (PseudoIdMap)query.getSingleResult();

        } catch (NoResultException ex) {
            return null;
        }
    }
}
