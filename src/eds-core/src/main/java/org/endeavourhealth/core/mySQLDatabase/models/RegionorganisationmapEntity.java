package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonOrganisation;
import org.endeavourhealth.coreui.json.JsonOrganisationManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "deleteRegionMapping",
                procedureName = "deleteRegionMapping",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "UUID")
                }
        ),
        @NamedStoredProcedureQuery(
                name = "deleteOrganisationMapping",
                procedureName = "deleteOrganisationMapping",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "UUID")
                }
        )
})
@Entity
@Table(name = "regionorganisationmap", schema = "organisationmanager")
@IdClass(RegionorganisationmapEntityPK.class)
public class RegionorganisationmapEntity {
    private String regionUuid;
    private String organisationUUid;
    private int id;

    @Id
    @Column(name = "regionUuid", nullable = false, length = 36)
    public String getRegionUuid() {
        return regionUuid;
    }

    public void setRegionUuid(String regionUuid) {
        this.regionUuid = regionUuid;
    }

    @Id
    @Column(name = "organisationUUid", nullable = false, length = 36)
    public String getOrganisationUUid() {
        return organisationUUid;
    }

    public void setOrganisationUUid(String organisationUUid) {
        this.organisationUUid = organisationUUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegionorganisationmapEntity that = (RegionorganisationmapEntity) o;

        if (regionUuid != null ? !regionUuid.equals(that.regionUuid) : that.regionUuid != null) return false;
        if (organisationUUid != null ? !organisationUUid.equals(that.organisationUUid) : that.organisationUUid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = regionUuid != null ? regionUuid.hashCode() : 0;
        result = 31 * result + (organisationUUid != null ? organisationUUid.hashCode() : 0);
        return result;
    }

    public static void saveRegionMappings(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> orgs = region.getOrganisations();
         orgs.forEach( (k,v) -> {
             RegionorganisationmapEntity rem = new RegionorganisationmapEntity();
             entityManager.getTransaction().begin();
             rem.setRegionUuid(region.getUuid());
             rem.setOrganisationUUid(k.toString());
             entityManager.persist(rem);
             entityManager.getTransaction().commit();
         });
    }

    public static void saveOrganisationMappings(JsonOrganisationManager organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> regions = organisation.getRegions();
        regions.forEach( (k,v) -> {
            RegionorganisationmapEntity rem = new RegionorganisationmapEntity();
            entityManager.getTransaction().begin();
            rem.setOrganisationUUid(organisation.getUuid());
            rem.setRegionUuid(k.toString());
            entityManager.persist(rem);
            entityManager.getTransaction().commit();
        });
    }

    public static void deleteRegionMap(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteRegionMapping");
        spq.setParameter("UUID", uuid);
        spq.execute();
        entityManager.close();
    }

    public static void deleteOrganisationMap(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteOrganisationMapping");
        spq.setParameter("UUID", uuid);
        spq.execute();
        entityManager.close();
    }
}
