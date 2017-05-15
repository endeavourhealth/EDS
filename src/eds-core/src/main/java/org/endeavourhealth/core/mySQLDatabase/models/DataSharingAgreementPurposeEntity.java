package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDsaPurpose;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by studu on 15/05/2017.
 */
@Entity
@Table(name = "DataSharingAgreementPurpose", schema = "OrganisationManager")
public class DataSharingAgreementPurposeEntity {
    private String uuid;
    private String dataSharingAgreementUuid;
    private String title;
    private String detail;

    @Id
    @Column(name = "Uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(name = "DataSharingAgreementUuid", nullable = false, length = 36)
    public String getDataSharingAgreementUuid() {
        return dataSharingAgreementUuid;
    }

    public void setDataSharingAgreementUuid(String dataSharingAgreementUuid) {
        this.dataSharingAgreementUuid = dataSharingAgreementUuid;
    }

    @Basic
    @Column(name = "Title", nullable = false, length = 50)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "Detail", nullable = false, length = 2000)
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSharingAgreementPurposeEntity that = (DataSharingAgreementPurposeEntity) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (dataSharingAgreementUuid != null ? !dataSharingAgreementUuid.equals(that.dataSharingAgreementUuid) : that.dataSharingAgreementUuid != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (detail != null ? !detail.equals(that.detail) : that.detail != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (dataSharingAgreementUuid != null ? dataSharingAgreementUuid.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (detail != null ? detail.hashCode() : 0);
        return result;
    }

    public static List<DataSharingAgreementPurposeEntity> getAllPurposes(String Uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSharingAgreementPurposeEntity> cq = cb.createQuery(DataSharingAgreementPurposeEntity.class);
        Root<DataSharingAgreementPurposeEntity> rootEntry = cq.from(DataSharingAgreementPurposeEntity.class);

        Predicate predicate = cb.equal(rootEntry.get("dataSharingAgreementUuid"), Uuid );

        cq.where(predicate);
        TypedQuery<DataSharingAgreementPurposeEntity> query = entityManager.createQuery(cq);

        List<DataSharingAgreementPurposeEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static void savePurpose(JsonDsaPurpose purpose) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementPurposeEntity dsaPurpose = new DataSharingAgreementPurposeEntity();
        dsaPurpose.setUuid(purpose.getUuid());
        dsaPurpose.setDetail(purpose.getDetail());
        dsaPurpose.setTitle(purpose.getTitle());
        dsaPurpose.setDataSharingAgreementUuid(purpose.getDataSharingAgreementUuid());
        entityManager.getTransaction().begin();
        entityManager.persist(dsaPurpose);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void updatePurpose(JsonDsaPurpose purpose) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementPurposeEntity dsaPurpose = entityManager.find(DataSharingAgreementPurposeEntity.class, purpose.getUuid());
        entityManager.getTransaction().begin();
        dsaPurpose.setTitle(purpose.getTitle());
        dsaPurpose.setDetail(purpose.getDetail());
        dsaPurpose.setDataSharingAgreementUuid(purpose.getDataSharingAgreementUuid());
        entityManager.getTransaction().commit();

        entityManager.close();
    }
}
