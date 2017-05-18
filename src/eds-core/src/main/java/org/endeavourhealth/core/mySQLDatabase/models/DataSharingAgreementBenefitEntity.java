package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDsaBenefit;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
@Table(name = "DataSharingAgreementBenefit", schema = "OrganisationManager")
public class DataSharingAgreementBenefitEntity {
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

        DataSharingAgreementBenefitEntity that = (DataSharingAgreementBenefitEntity) o;

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

    public static List<DataSharingAgreementBenefitEntity> getAllBenefits(String Uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSharingAgreementBenefitEntity> cq = cb.createQuery(DataSharingAgreementBenefitEntity.class);
        Root<DataSharingAgreementBenefitEntity> rootEntry = cq.from(DataSharingAgreementBenefitEntity.class);

        Predicate predicate = cb.equal(rootEntry.get("dataSharingAgreementUuid"), Uuid );

        cq.where(predicate);
        TypedQuery<DataSharingAgreementBenefitEntity> query = entityManager.createQuery(cq);

        List<DataSharingAgreementBenefitEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static void saveBenefit(JsonDsaBenefit benefit) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementBenefitEntity dsaPurpose = new DataSharingAgreementBenefitEntity();
        dsaPurpose.setUuid(benefit.getUuid());
        dsaPurpose.setDetail(benefit.getDetail());
        dsaPurpose.setTitle(benefit.getTitle());
        dsaPurpose.setDataSharingAgreementUuid(benefit.getDataSharingAgreementUuid());
        entityManager.getTransaction().begin();
        entityManager.persist(dsaPurpose);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void updateBenefit(JsonDsaBenefit benefit) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementBenefitEntity dsaPurpose = entityManager.find(DataSharingAgreementBenefitEntity.class, benefit.getUuid());
        entityManager.getTransaction().begin();
        dsaPurpose.setTitle(benefit.getTitle());
        dsaPurpose.setDetail(benefit.getDetail());
        dsaPurpose.setDataSharingAgreementUuid(benefit.getDataSharingAgreementUuid());
        entityManager.getTransaction().commit();

        entityManager.close();
    }
}
