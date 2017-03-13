package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "getAllRegions",
                procedureName = "getAllRegions"
        )
})

@Table(name = "region", schema = "organisationmanager")
public class RegionEntity {
    private int id;
    private String name;
    private String description;
    private String uuid;

    public static List<Object[]> getAllRegions() throws Exception {

        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("getAllRegions");
        spq.execute();
        List<Object[]> ent = spq.getResultList();
        entityManager.close();

        return ent;
    }

    public static RegionEntity getSingleRegion(String uuid) throws Exception {

        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        return entityManager.find(RegionEntity.class, uuid);

    }

    public static boolean updateRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        RegionEntity re = entityManager.find(RegionEntity.class, region.getUuid());
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        entityManager.getTransaction().commit();

        return true;

    }

    public static boolean saveRegion(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        RegionEntity re = new RegionEntity();
        entityManager.getTransaction().begin();
        re.setDescription(region.getDescription());
        re.setName(region.getName());
        re.setUuid(UUID.randomUUID().toString());
        entityManager.persist(re);
        entityManager.getTransaction().commit();

        return true;

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
    @Column(name = "Uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
