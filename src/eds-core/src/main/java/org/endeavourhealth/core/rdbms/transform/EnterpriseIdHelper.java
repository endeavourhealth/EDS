package org.endeavourhealth.core.rdbms.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

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

        if (resourceId == null) {
            throw new IllegalArgumentException("Null resource ID");
        }

        EnterpriseIdMap mapping = new EnterpriseIdMap();
        mapping.setEnterpriseTableName(enterpriseTableName);
        mapping.setResourceType(resourceType);
        mapping.setResourceId(resourceId);
        //mapping.setEnterpriseId(new Long(0));

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(mapping);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            entityManager.getTransaction().rollback();
            throw ex;
        }

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

    public static void saveEnterpriseOrganisationId(String serviceId, String systemId, String configName, Long enterpriseId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        try {
            EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(serviceId, systemId, configName, entityManager);
            if (mapping != null) {
                throw new Exception("EnterpriseOrganisationIdMap already exists for service " + serviceId + " system " + systemId + " config " + configName);
            }

            mapping = new EnterpriseOrganisationIdMap();
            mapping.setServiceId(serviceId);
            mapping.setSystemId(systemId);
            mapping.setEnterpriseConfigName(configName);
            mapping.setEnterpriseId(enterpriseId);

            entityManager.getTransaction().begin();
            entityManager.persist(mapping);
            entityManager.getTransaction().commit();

        } finally {
            entityManager.close();
        }
    }

    private static EnterpriseOrganisationIdMap findEnterpriseOrganisationMapping(String serviceId, String systemId, String configName,  EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseOrganisationIdMap c"
                + " where c.serviceId = :serviceId"
                + " and c.systemId = :systemId"
                + " and c.enterpriseConfigName = :enterpriseConfigName";

        Query query = entityManager.createQuery(sql, EnterpriseOrganisationIdMap.class)
                .setParameter("serviceId", serviceId)
                .setParameter("systemId", systemId)
                .setParameter("enterpriseConfigName", configName);

        try {
            EnterpriseOrganisationIdMap result = (EnterpriseOrganisationIdMap)query.getSingleResult();
            return result;

        } catch (NoResultException ex) {
            return null;
        }
    }

    public static Long findEnterpriseOrganisationId(String serviceId, String systemId, String configName) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();
        EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(serviceId, systemId, configName, entityManager);
        entityManager.close();
        if (mapping != null) {
            return mapping.getEnterpriseId();
        } else {
            return null;
        }
    }

    /*public static void saveEnterpriseOrganisationId(String odsCode, Long enterpriseId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        try {
            EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(odsCode, entityManager);
            if (mapping != null) {
                mapping.setEnterpriseId(enterpriseId);

                entityManager.getTransaction().begin();
                entityManager.persist(mapping);
                entityManager.getTransaction().commit();
            }
        } finally {
            entityManager.close();
        }
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
    }*/

    public static Long findOrCreateEnterprisePersonId(String discoveryPersonId, String enterpriseConfigName) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        Long ret = findEnterprisePersonId(discoveryPersonId, enterpriseConfigName, entityManager);
        if (ret != null) {
            entityManager.close();
            return ret;
        }

        try {
            return createEnterprisePersonId(discoveryPersonId, enterpriseConfigName, entityManager);

        } catch (Exception ex) {
            //if another thread has beat us to it, we'll get an exception, so try the find again
            ret = findEnterprisePersonId(discoveryPersonId, enterpriseConfigName, entityManager);
            if (ret != null) {
                return ret;
            }

            throw ex;
        } finally {
            entityManager.close();
        }
    }

    private static Long findEnterprisePersonId(String discoveryPersonId, String enterpriseConfigName, EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " EnterprisePersonIdMap c"
                + " where c.personId = :personId"
                + " and c.enterpriseConfigName = :enterpriseConfigName";


        Query query = entityManager.createQuery(sql, EnterprisePersonIdMap.class)
                .setParameter("personId", discoveryPersonId)
                .setParameter("enterpriseConfigName", enterpriseConfigName);

        try {
            EnterprisePersonIdMap result = (EnterprisePersonIdMap)query.getSingleResult();
            return result.getEnterprisePersonId();

        } catch (NoResultException ex) {
            return null;
        }
    }

    private static Long createEnterprisePersonId(String discoveryPersonId, String enterpriseConfigName, EntityManager entityManager) throws Exception {

        EnterprisePersonIdMap mapping = new EnterprisePersonIdMap();
        mapping.setPersonId(discoveryPersonId);
        mapping.setEnterpriseConfigName(enterpriseConfigName);

        entityManager.getTransaction().begin();
        entityManager.persist(mapping);
        entityManager.getTransaction().commit();

        return mapping.getEnterprisePersonId();
    }

    public static Long findEnterprisePersonId(String discoveryPersonId, String enterpriseConfigName) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager();
        try {
            return findEnterprisePersonId(discoveryPersonId, enterpriseConfigName, entityManager);
        } finally {
            entityManager.close();
        }
    }

    public static List<EnterprisePersonIdMap> findEnterprisePersonMapsForPersonId(String discoveryPersonId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        String sql = "select c"
                + " from"
                + " EnterprisePersonIdMap c"
                + " where c.personId = :personId";


        Query query = entityManager.createQuery(sql, EnterprisePersonIdMap.class)
                .setParameter("personId", discoveryPersonId);

        try {
            List<EnterprisePersonIdMap> ret = query.getResultList();
            return ret;

        } finally {
            entityManager.close();
        }
    }
}
