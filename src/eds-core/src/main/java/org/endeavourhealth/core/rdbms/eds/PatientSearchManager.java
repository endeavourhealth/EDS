package org.endeavourhealth.core.rdbms.eds;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigEntity;
import org.endeavourhealth.common.config.ConfigManager;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.*;

public class PatientSearchManager {
    private static final Logger LOG = LoggerFactory.getLogger(PatientSearchManager.class);

    private static EntityManagerFactory entityManager;


    public static void update(UUID serviceId, UUID systemId, Patient fhirPatient) throws Exception {

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();

        PatientSearch patientSearch = new PatientSearch();
        patientSearch.setSystemId(systemId.toString());
        patientSearch.setServiceId(serviceId.toString());
        patientSearch.setPatientId(UUID.randomUUID().toString());
        patientSearch.setForenames("forename");
        patientSearch.setNhsNumber("1111111111");
        patientSearch.setSurname("surname");
        patientSearch.setLastUpdated(new Date());

        entityManager.persist(patientSearch);
        //entityManager.merge(patientSearch);

        entityManager.getTransaction().commit();
        entityManager.close();
    }


    public static List<PatientSearch> searchByNhsNumber(UUID serviceId, String nhsNumber) throws Exception {
        EntityManager entityManager = getEntityManager();

        String sql =
                " select c" +
                        " from " +
                        "    PatientSearch c" +
                        " where c.service_id = :service_id" +
                        " and   c.nhs_number = :nhs_number";

        Query query = entityManager.createQuery(sql, ConfigEntity.class)
                .setParameter("service_id", serviceId)
                .setParameter("nhs_number", nhsNumber);

        List<PatientSearch> results = query.getResultList();
        entityManager.close();
        return results;
    }

    //search NHS number
    //search local number
    //search DoB (and surname?)
    //search surname (and forenames)


    private static EntityManager getEntityManager() throws Exception {

        if (entityManager == null
                || !entityManager.isOpen()) {
            createEntityManager();
        }

        return entityManager.createEntityManager();
    }

    private static synchronized void createEntityManager() throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("eds_db");
        String url = json.get("url").asText();
        String user = json.get("username").asText();
        String pass = json.get("password").asText();

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hikari.dataSource.url", url);
        properties.put("hibernate.hikari.dataSource.user", user);
        properties.put("hibernate.hikari.dataSource.password", pass);

        entityManager = Persistence.createEntityManagerFactory("EdsDb", properties);
    }
}
