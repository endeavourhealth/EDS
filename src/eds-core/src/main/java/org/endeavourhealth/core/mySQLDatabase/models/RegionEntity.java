package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Region", schema = "OrganisationManager")
public class RegionEntity {
    private int id;
    private String name;
    private String description;
    private String uuid;

    public static List<RegionEntity> getAllRegions() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
        Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);
        CriteriaQuery<RegionEntity> all = cq.select(rootEntry);
        TypedQuery<RegionEntity> allQuery = entityManager.createQuery(all);
        List<RegionEntity> ret = allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static RegionEntity getSingleRegion(String uuid) throws Exception {

        EntityManager entityManager = PersistenceManager.getEntityManager();

        RegionEntity ret = entityManager.find(RegionEntity.class, uuid);

        entityManager.close();

        return ret;

    }

    public static void updateRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        RegionEntity re = entityManager.find(RegionEntity.class, region.getUuid());
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        RegionEntity re = new RegionEntity();
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        re.setUuid(region.getUuid());
        entityManager.persist(re);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteRegion(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        RegionEntity re = entityManager.find(RegionEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(re);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<RegionEntity> getRegionsFromList(List<String> regions) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
        Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(regions);

        cq.where(predicate);
        TypedQuery<RegionEntity> query = entityManager.createQuery(cq);

        List<RegionEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<RegionEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegionEntity> cq = cb.createQuery(RegionEntity.class);
        Root<RegionEntity> rootEntry = cq.from(RegionEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<RegionEntity> query = entityManager.createQuery(cq);
        List<RegionEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "description", nullable = true, length = 2000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegionEntity that = (RegionEntity) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Id
    @Column(name = "uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
