package org.endeavourhealth.core.rdbms.transform;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnterpriseIdHelper {
    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseIdHelper.class);


    public static Long findOrCreateEnterpriseId(String enterpriseConfigName, String resourceType, String resourceId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        Long ret = findEnterpriseId(resourceType, resourceId, entityManager);
        if (ret != null) {
            entityManager.close();
            return ret;
        }

        try {
            return createEnterpriseId(resourceType, resourceId, entityManager);

        } catch (Exception ex) {
            //if another thread has beat us to it, we'll get an exception, so try the find again
            ret = findEnterpriseId(resourceType, resourceId, entityManager);
            if (ret != null) {
                return ret;
            }

            throw ex;
        } finally {
            entityManager.close();
        }
    }

    private static Long createEnterpriseId(String resourceType, String resourceId, EntityManager entityManager) throws Exception {

        if (resourceId == null) {
            throw new IllegalArgumentException("Null resource ID");
        }

        EnterpriseIdMap mapping = new EnterpriseIdMap();
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

    public static Long findEnterpriseId(String enterpriseConfigName, String resourceType, String resourceId) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);
        try {
            return findEnterpriseId(resourceType, resourceId, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private static Long findEnterpriseId(String resourceType, String resourceId, EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseIdMap c"
                + " where c.resourceType = :resourceType"
                + " and c.resourceId = :resourceId";


        Query query = entityManager.createQuery(sql, EnterpriseIdMap.class)
                .setParameter("resourceType", resourceType)
                .setParameter("resourceId", resourceId);

        try {
            EnterpriseIdMap result = (EnterpriseIdMap)query.getSingleResult();
            return result.getEnterpriseId();

        } catch (NoResultException ex) {
            return null;
        }
    }

    public static void saveEnterpriseOrganisationId(String serviceId, String systemId, String enterpriseConfigName, Long enterpriseId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        try {
            EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(serviceId, systemId, entityManager);
            if (mapping != null) {
                throw new Exception("EnterpriseOrganisationIdMap already exists for service " + serviceId + " system " + systemId + " config " + enterpriseConfigName);
            }

            mapping = new EnterpriseOrganisationIdMap();
            mapping.setServiceId(serviceId);
            mapping.setSystemId(systemId);
            //mapping.setEnterpriseConfigName(configName);
            mapping.setEnterpriseId(enterpriseId);

            entityManager.getTransaction().begin();
            entityManager.persist(mapping);
            entityManager.getTransaction().commit();

        } finally {
            entityManager.close();
        }
    }

    private static EnterpriseOrganisationIdMap findEnterpriseOrganisationMapping(String serviceId, String systemId, EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseOrganisationIdMap c"
                + " where c.serviceId = :serviceId"
                + " and c.systemId = :systemId";

        Query query = entityManager.createQuery(sql, EnterpriseOrganisationIdMap.class)
                .setParameter("serviceId", serviceId)
                .setParameter("systemId", systemId);

        try {
            EnterpriseOrganisationIdMap result = (EnterpriseOrganisationIdMap)query.getSingleResult();
            return result;

        } catch (NoResultException ex) {
            return null;
        }
    }

    public static Long findEnterpriseOrganisationId(String serviceId, String systemId, String enterpriseConfigName) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);
        EnterpriseOrganisationIdMap mapping = findEnterpriseOrganisationMapping(serviceId, systemId, entityManager);
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

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

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

    public static Long findEnterprisePersonId(String discoveryPersonId, String enterpriseConfigName) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);
        try {
            return findEnterprisePersonId(discoveryPersonId, entityManager);
        } finally {
            entityManager.close();
        }
    }

    public static List<EnterprisePersonIdMap> findEnterprisePersonMapsForPersonId(String enterpriseConfigName, String discoveryPersonId) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

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

    public static void findEnterpriseIds(String enterpriseConfigName, List<ResourceByExchangeBatch> resources, Map<ResourceByExchangeBatch, Long> ids) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);
        try {
            findEnterpriseIds(resources, ids, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private static void findEnterpriseIds(List<ResourceByExchangeBatch> resources, Map<ResourceByExchangeBatch, Long> ids, EntityManager entityManager) throws Exception {

        String resourceType = null;
        List<String> resourceIds = new ArrayList<>();
        Map<String, ResourceByExchangeBatch> resourceIdMap = new HashMap<>();

        for (ResourceByExchangeBatch resource: resources) {

            if (resourceType == null) {
                resourceType = resource.getResourceType();
            } else if (!resourceType.equals(resource.getResourceType())) {
                throw new Exception("Can't find enterprise IDs for different resource types");
            }

            String id = resource.getResourceId().toString();
            resourceIds.add(id);
            resourceIdMap.put(id, resource);
        }

        String sql = "select c"
                + " from"
                + " EnterpriseIdMap c"
                + " where c.resourceType = :resourceType"
                + " and c.resourceId IN :resourceId";


        Query query = entityManager.createQuery(sql, EnterpriseIdMap.class)
                .setParameter("resourceType", resourceType)
                .setParameter("resourceId", resourceIds);

        List<EnterpriseIdMap> results = query.getResultList();
        for (EnterpriseIdMap result: results) {
            String resourceId = result.getResourceId();
            Long enterpriseId = result.getEnterpriseId();

            ResourceByExchangeBatch resource = resourceIdMap.get(resourceId);
            ids.put(resource, enterpriseId);
        }
    }

    public static void findOrCreateEnterpriseIds(String enterpriseConfigName, List<ResourceByExchangeBatch> resources, Map<ResourceByExchangeBatch, Long> ids) throws Exception {
        EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);

        //check the DB for existing IDs
        findEnterpriseIds(resources, ids, entityManager);

        //find the resources that didn't have an ID
        List<ResourceByExchangeBatch> resourcesToCreate = new ArrayList<>();
        for (ResourceByExchangeBatch resource: resources) {
            if (!ids.containsKey(resource)) {
                resourcesToCreate.add(resource);
            }
        }

        //for any resource without an ID, we want to create one
        try {
            entityManager.getTransaction().begin();

            Map<ResourceByExchangeBatch, EnterpriseIdMap> mappingMap = new HashMap<>();

            for (ResourceByExchangeBatch resource: resourcesToCreate) {

                EnterpriseIdMap mapping = new EnterpriseIdMap();
                mapping.setResourceType(resource.getResourceType());
                mapping.setResourceId(resource.getResourceId().toString());

                entityManager.persist(mapping);

                mappingMap.put(resource, mapping);
            }

            entityManager.getTransaction().commit();

            for (ResourceByExchangeBatch resource: resourcesToCreate) {

                EnterpriseIdMap mapping = mappingMap.get(resource);
                Long enterpriseId = mapping.getEnterpriseId();
                ids.put(resource, enterpriseId);
            }

        } catch (Exception ex) {
            //if another thread has beat us to it and created an ID for one of our records and we'll get an exception, so try the find again
            //but for each one individually
            entityManager.getTransaction().rollback();
            LOG.warn("Failed to create " + resourcesToCreate.size() + " IDs in one go, so doing one by one");

            for (ResourceByExchangeBatch resource: resourcesToCreate) {
                Long enterpriseId = findOrCreateEnterpriseId(enterpriseConfigName, resource.getResourceType(), resource.getResourceId().toString());
                ids.put(resource, enterpriseId);
            }

        } finally {
            entityManager.close();
        }
    }
}
