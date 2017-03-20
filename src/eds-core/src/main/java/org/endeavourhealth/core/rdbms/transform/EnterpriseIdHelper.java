package org.endeavourhealth.core.rdbms.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class EnterpriseIdHelper {
    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseIdHelper.class);


    public static Long findOrCreateEnterpriseId(String enterpriseTableName, String resourceType, String resourceId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        Long ret = findEnterpriseId(enterpriseTableName, resourceType, resourceId, entityManager);
        if (ret != null) {
            entityManager.close();
            return ret;
        }

        try {
            return createEnterpriseId(enterpriseTableName, resourceType, resourceId, entityManager);

        } catch (Exception ex) {
            //if another thread has beat us to it, we'll get an exception, so try the find again
            ret = findEnterpriseId(enterpriseTableName, resourceType, resourceId, entityManager);
            if (ret != null) {
                return ret;
            }

            throw ex;
        } finally {
            entityManager.close();
        }
    }

    private static Long createEnterpriseId(String enterpriseTableName, String resourceType, String resourceId, EntityManager entityManager) throws Exception {

        EnterpriseIdMap mapping = new EnterpriseIdMap();
        mapping.setEnterpriseTableName(enterpriseTableName);
        mapping.setResourceType(resourceType);
        mapping.setResourceId(resourceId);
        //mapping.setEnterpriseId(new Long(0));

        entityManager.getTransaction().begin();
        entityManager.persist(mapping);
        entityManager.getTransaction().commit();

        return mapping.getEnterpriseId();
    }

    public static Long findEnterpriseId(String enterpriseTableName, String resourceType, String resourceId) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager();
        try {
            return findEnterpriseId(enterpriseTableName, resourceType, resourceId, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private static Long findEnterpriseId(String enterpriseTableName, String resourceType, String resourceId, EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseIdMap c"
                + " where c.enterpriseTableName = :enterpriseTableName"
                + " and c.resourceType = :resourceType"
                + " and c.resourceId = :resourceId";


        Query query = entityManager.createQuery(sql, EnterpriseIdMap.class)
                .setParameter("enterpriseTableName", enterpriseTableName)
                .setParameter("resourceType", resourceType)
                .setParameter("resourceId", resourceId);

        try {
            EnterpriseIdMap result = (EnterpriseIdMap)query.getSingleResult();
            return result.getEnterpriseId();

        } catch (NoResultException ex) {
            return null;
        }
    }

    public static void saveEnterpriseOrganisationId(String odsCode, Long enterpriseId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();
        EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(odsCode, entityManager);
        if (mapping == null) {
            mapping = new EnterpriseOrganisationIdMap();
            mapping.setOdsCode(odsCode);
        }

        mapping.setEnterpriseId(enterpriseId);

        entityManager.getTransaction().begin();
        entityManager.persist(mapping);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static EnterpriseOrganisationIdMap findEnterpriseOrganisationMapping(String odsCode,  EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseOrganisationIdMap c"
                + " where c.odsCode = :odsCode";

        Query query = entityManager.createQuery(sql, EnterpriseOrganisationIdMap.class)
                .setParameter("odsCode", odsCode);

        try {
            EnterpriseOrganisationIdMap result = (EnterpriseOrganisationIdMap)query.getSingleResult();
            return result;

        } catch (NoResultException ex) {
            return null;
        }
    }

    public static Long findEnterpriseOrganisationId(String odsCode) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();
        EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(odsCode, entityManager);
        entityManager.close();
        if (mapping != null) {
            return mapping.getEnterpriseId();
        } else {
            return null;
        }
    }

    public static Long findOrCreateEnterprisePersonId(String discoveryPersonId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        Long ret = findEnterprisePersonId(discoveryPersonId, entityManager);
        if (ret != null) {
            entityManager.close();
            return ret;
        }

        try {
            return createEnterprisePersonId(discoveryPersonId, entityManager);

        } catch (Exception ex) {
            //if another thread has beat us to it, we'll get an exception, so try the find again
            ret = findEnterprisePersonId(discoveryPersonId, entityManager);
            if (ret != null) {
                return ret;
            }

            throw ex;
        } finally {
            entityManager.close();
        }
    }

    private static Long findEnterprisePersonId(String discoveryPersonId, EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " EnterprisePersonIdMap c"
                + " where c.personId = :personId";


        Query query = entityManager.createQuery(sql, EnterprisePersonIdMap.class)
                .setParameter("personId", discoveryPersonId);

        try {
            EnterprisePersonIdMap result = (EnterprisePersonIdMap)query.getSingleResult();
            return result.getEnterprisePersonId();

        } catch (NoResultException ex) {
            return null;
        }
    }

    private static Long createEnterprisePersonId(String discoveryPersonId, EntityManager entityManager) throws Exception {

        EnterprisePersonIdMap mapping = new EnterprisePersonIdMap();
        mapping.setPersonId(discoveryPersonId);

        entityManager.getTransaction().begin();
        entityManager.persist(mapping);
        entityManager.getTransaction().commit();

        return mapping.getEnterprisePersonId();
    }

    public static Long findEnterprisePersonId(String discoveryPersonId) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager();
        try {
            return findEnterprisePersonId(discoveryPersonId, entityManager);
        } finally {
            entityManager.close();
        }
    }
}
