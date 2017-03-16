package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonCohort;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cohort", schema = "organisationmanager")
public class CohortEntity {
    private String uuid;
    private String name;
    private String nature;
    private String patientCohortInclusionConsentModel;
    private String queryDefinition;
    private String removalPolicy;

    @Id
    @Column(name = "Uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(name = "Name", nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "Nature", nullable = true, length = 100)
    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    @Basic
    @Column(name = "PatientCohortInclusionConsentModel", nullable = true, length = 100)
    public String getPatientCohortInclusionConsentModel() {
        return patientCohortInclusionConsentModel;
    }

    public void setPatientCohortInclusionConsentModel(String patientCohortInclusionConsentModel) {
        this.patientCohortInclusionConsentModel = patientCohortInclusionConsentModel;
    }

    @Basic
    @Column(name = "QueryDefinition", nullable = true, length = 100)
    public String getQueryDefinition() {
        return queryDefinition;
    }

    public void setQueryDefinition(String queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    @Basic
    @Column(name = "RemovalPolicy", nullable = true, length = 100)
    public String getRemovalPolicy() {
        return removalPolicy;
    }

    public void setRemovalPolicy(String removalPolicy) {
        this.removalPolicy = removalPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CohortEntity that = (CohortEntity) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (nature != null ? !nature.equals(that.nature) : that.nature != null) return false;
        if (patientCohortInclusionConsentModel != null ? !patientCohortInclusionConsentModel.equals(that.patientCohortInclusionConsentModel) : that.patientCohortInclusionConsentModel != null)
            return false;
        if (queryDefinition != null ? !queryDefinition.equals(that.queryDefinition) : that.queryDefinition != null)
            return false;
        if (removalPolicy != null ? !removalPolicy.equals(that.removalPolicy) : that.removalPolicy != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (nature != null ? nature.hashCode() : 0);
        result = 31 * result + (patientCohortInclusionConsentModel != null ? patientCohortInclusionConsentModel.hashCode() : 0);
        result = 31 * result + (queryDefinition != null ? queryDefinition.hashCode() : 0);
        result = 31 * result + (removalPolicy != null ? removalPolicy.hashCode() : 0);
        return result;
    }

    public static List<CohortEntity> getAllCohorts() throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
        Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);
        CriteriaQuery<CohortEntity> all = cq.select(rootEntry);
        TypedQuery<CohortEntity> allQuery = entityManager.createQuery(all);
        return allQuery.getResultList();
    }

    public static CohortEntity getCohort(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        return entityManager.find(CohortEntity.class, uuid);
    }

    public static void updateCohort(JsonCohort cohort) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CohortEntity cohortEntity = entityManager.find(CohortEntity.class, cohort.getUuid());
        entityManager.getTransaction().begin();
        cohortEntity.setName(cohort.getName());
        cohortEntity.setNature(cohort.getNature());
        cohortEntity.setPatientCohortInclusionConsentModel(cohort.getPatientCohortInclusionConsentModel());
        cohortEntity.setQueryDefinition(cohort.getQueryDefinition());
        cohortEntity.setRemovalPolicy(cohort.getRemovalPolicy());
        entityManager.getTransaction().commit();
    }

    public static void saveCohort(JsonCohort cohort) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CohortEntity cohortEntity = new CohortEntity();
        entityManager.getTransaction().begin();
        cohortEntity.setName(cohort.getName());
        cohortEntity.setNature(cohort.getNature());
        cohortEntity.setPatientCohortInclusionConsentModel(cohort.getPatientCohortInclusionConsentModel());
        cohortEntity.setQueryDefinition(cohort.getQueryDefinition());
        cohortEntity.setRemovalPolicy(cohort.getRemovalPolicy());
        cohortEntity.setUuid(UUID.randomUUID().toString());
        entityManager.persist(cohortEntity);
        entityManager.getTransaction().commit();
    }

    public static void deleteCohort(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CohortEntity cohortEntity = entityManager.find(CohortEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(cohortEntity);
        entityManager.getTransaction().commit();
    }

    public static List<CohortEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CohortEntity> cq = cb.createQuery(CohortEntity.class);
        Root<CohortEntity> rootEntry = cq.from(CohortEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("nature")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<CohortEntity> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
