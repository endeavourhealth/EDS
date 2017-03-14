package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonOrganisationManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "deleteSupraRegionParentMapping",
                procedureName = "deleteSupraRegionParentMapping",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "UUID")
                }
        ),
        @NamedStoredProcedureQuery(
                name = "deleteSupraRegionChildMapping",
                procedureName = "deleteSupraRegionChildMapping",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "UUID")
                }
        )
})
@Entity
@Table(name = "supraregionmap", schema = "organisationmanager")
@IdClass(SupraregionmapEntityPK.class)
public class SupraregionmapEntity {
    private String parentRegionUuid;
    private String childRegionUUid;

    @Id
    @Column(name = "ParentRegionUuid", nullable = false, length = 36)
    public String getParentRegionUuid() {
        return parentRegionUuid;
    }

    public void setParentRegionUuid(String parentRegionUuid) {
        this.parentRegionUuid = parentRegionUuid;
    }

    @Id
    @Column(name = "ChildRegionUUid", nullable = false, length = 36)
    public String getChildRegionUUid() {
        return childRegionUUid;
    }

    public void setChildRegionUUid(String childRegionUUid) {
        this.childRegionUUid = childRegionUUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupraregionmapEntity that = (SupraregionmapEntity) o;

        if (parentRegionUuid != null ? !parentRegionUuid.equals(that.parentRegionUuid) : that.parentRegionUuid != null)
            return false;
        if (childRegionUUid != null ? !childRegionUUid.equals(that.childRegionUUid) : that.childRegionUUid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentRegionUuid != null ? parentRegionUuid.hashCode() : 0;
        result = 31 * result + (childRegionUUid != null ? childRegionUUid.hashCode() : 0);
        return result;
    }

    public static void deleteSupraRegionParentMapping(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteSupraRegionParentMapping");
        spq.setParameter("UUID", uuid);
        spq.execute();
        entityManager.close();
    }

    public static void deleteSupraRegionChildMapping(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteSupraRegionChildMapping");
        spq.setParameter("UUID", uuid);
        spq.execute();
        entityManager.close();
    }


    public static void saveSupraRegionParentMappings(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> orgs = region.getChildRegions();
        orgs.forEach( (k,v) -> {
            SupraregionmapEntity srm = new SupraregionmapEntity();
            entityManager.getTransaction().begin();
            srm.setParentRegionUuid(region.getUuid());
            srm.setChildRegionUUid(k.toString());
            entityManager.persist(srm);
            entityManager.getTransaction().commit();
        });
    }

    public static void saveSupraRegionChildMappings(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> orgs = region.getParentRegions();
        orgs.forEach( (k,v) -> {
            SupraregionmapEntity srm = new SupraregionmapEntity();
            entityManager.getTransaction().begin();
            srm.setChildRegionUUid(region.getUuid());
            srm.setParentRegionUuid(k.toString());
            entityManager.persist(srm);
            entityManager.getTransaction().commit();
        });
    }
}
