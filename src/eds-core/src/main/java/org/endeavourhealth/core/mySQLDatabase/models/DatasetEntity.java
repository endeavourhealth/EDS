package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDataSet;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
@Table(name = "Dataset", schema = "OrganisationManager")
public class DatasetEntity {
    private String uuid;
    private String name;
    private String description;
    private String attributes;
    private String queryDefinition;

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
    @Column(name = "Description", nullable = true, length = 100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "Attributes", nullable = false, length = -1)
    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    @Basic
    @Column(name = "QueryDefinition", nullable = true, length = 100)
    public String getQueryDefinition() {
        return queryDefinition;
    }

    public void setQueryDefinition(String queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatasetEntity that = (DatasetEntity) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
        if (queryDefinition != null ? !queryDefinition.equals(that.queryDefinition) : that.queryDefinition != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (queryDefinition != null ? queryDefinition.hashCode() : 0);
        return result;
    }

    public static List<DatasetEntity> getAllDataSets() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
        Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);
        CriteriaQuery<DatasetEntity> all = cq.select(rootEntry);
        TypedQuery<DatasetEntity> allQuery = entityManager.createQuery(all);
        List<DatasetEntity> ret = allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<DatasetEntity> getDataSetsFromList(List<String> datasets) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
        Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(datasets);

        cq.where(predicate);
        TypedQuery<DatasetEntity> query = entityManager.createQuery(cq);

        List<DatasetEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static DatasetEntity getDataSet(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DatasetEntity ret = entityManager.find(DatasetEntity.class, uuid);
        entityManager.close();

        return ret;
    }

    public static void updateDataSet(JsonDataSet dataset) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DatasetEntity dataSetEntity = entityManager.find(DatasetEntity.class, dataset.getUuid());
        entityManager.getTransaction().begin();
        dataSetEntity.setName(dataset.getName());
        dataSetEntity.setDescription(dataset.getDescription());
        dataSetEntity.setAttributes(dataset.getAttributes());
        dataSetEntity.setQueryDefinition(dataset.getQueryDefinition());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveDataSet(JsonDataSet dataset) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DatasetEntity dataSetEntity = new DatasetEntity();
        entityManager.getTransaction().begin();
        dataSetEntity.setUuid(dataset.getUuid());
        dataSetEntity.setName(dataset.getName());
        dataSetEntity.setDescription(dataset.getDescription());
        dataSetEntity.setAttributes(dataset.getAttributes());
        dataSetEntity.setQueryDefinition(dataset.getQueryDefinition());
        entityManager.persist(dataSetEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteDataSet(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DatasetEntity dataSetEntity = entityManager.find(DatasetEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dataSetEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<DatasetEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasetEntity> cq = cb.createQuery(DatasetEntity.class);
        Root<DatasetEntity> rootEntry = cq.from(DatasetEntity.class);

        Predicate predicate = cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%");

        cq.where(predicate);
        TypedQuery<DatasetEntity> query = entityManager.createQuery(cq);
        List<DatasetEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
