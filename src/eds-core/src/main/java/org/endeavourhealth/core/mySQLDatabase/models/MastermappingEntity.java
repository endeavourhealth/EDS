package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonOrganisationManager;
import org.endeavourhealth.coreui.json.JsonRegion;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
            name = "deleteAllMappings",
            procedureName = "deleteAllMappings",
            parameters = {
                    @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "UUID")
            }
    )
})
@Entity
@Table(name = "mastermapping", schema = "organisationmanager")
@IdClass(MastermappingEntityPK.class)
public class MastermappingEntity {
    private String childUuid;
    private String parentUUid;
    private short mapTypeId;
    private byte isDefault;

    @Id
    @Column(name = "ChildUuid", nullable = false, length = 36)
    public String getChildUuid() {
        return childUuid;
    }

    public void setChildUuid(String childUuid) {
        this.childUuid = childUuid;
    }

    @Id
    @Column(name = "ParentUUid", nullable = false, length = 36)
    public String getParentUUid() {
        return parentUUid;
    }

    public void setParentUUid(String parentUUid) {
        this.parentUUid = parentUUid;
    }

    @Id
    @Column(name = "MapTypeId", nullable = false)
    public short getMapTypeId() {
        return mapTypeId;
    }

    public void setMapTypeId(short mapTypeId) {
        this.mapTypeId = mapTypeId;
    }

    @Basic
    @Column(name = "IsDefault", nullable = false)
    public byte getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(byte isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MastermappingEntity that = (MastermappingEntity) o;

        if (mapTypeId != that.mapTypeId) return false;
        if (isDefault != that.isDefault) return false;
        if (childUuid != null ? !childUuid.equals(that.childUuid) : that.childUuid != null) return false;
        if (parentUUid != null ? !parentUUid.equals(that.parentUUid) : that.parentUUid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = childUuid != null ? childUuid.hashCode() : 0;
        result = 31 * result + (parentUUid != null ? parentUUid.hashCode() : 0);
        result = 31 * result + (int) mapTypeId;
        result = 31 * result + (int) isDefault;
        return result;
    }

    public static void deleteAllMappings(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteAllMappings");
        spq.setParameter("UUID", uuid);
        spq.execute();
        entityManager.close();
    }

    public static void saveOrganisationMappings(JsonOrganisationManager organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> regions = organisation.getRegions();
        regions.forEach( (k,v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(organisation.getUuid());
            mme.setParentUUid(k.toString());
            mme.setMapTypeId((short)1);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        Map<UUID, String> parentOrganisations = organisation.getParentOrganisations();
        parentOrganisations.forEach( (k,v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(organisation.getUuid());
            mme.setParentUUid(k.toString());
            mme.setMapTypeId((short)0);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        Map<UUID, String> childOrganisations = organisation.getChildOrganisations();
        childOrganisations.forEach( (k,v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(k.toString());
            mme.setParentUUid(organisation.getUuid());
            mme.setMapTypeId((short)0);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        Map<UUID, String> services = organisation.getServices();
        services.forEach( (k,v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(k.toString());
            mme.setParentUUid(organisation.getUuid());
            mme.setMapTypeId((short)0);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });
    }

    public static void saveRegionMappings(JsonRegion region) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        Map<UUID, String> parentRegions = region.getParentRegions();
        parentRegions.forEach((k, v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(region.getUuid());
            mme.setParentUUid(k.toString());
            mme.setMapTypeId((short) 1);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        Map<UUID, String> childRegions = region.getChildRegions();
        childRegions.forEach((k, v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(k.toString());
            mme.setParentUUid(region.getUuid());
            mme.setMapTypeId((short) 1);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });

        Map<UUID, String> organisations = region.getOrganisations();
        organisations.forEach((k, v) -> {
            MastermappingEntity mme = new MastermappingEntity();
            entityManager.getTransaction().begin();
            mme.setChildUuid(k.toString());
            mme.setParentUUid(region.getUuid());
            mme.setMapTypeId((short) 1);
            entityManager.persist(mme);
            entityManager.getTransaction().commit();
        });
    }

}
