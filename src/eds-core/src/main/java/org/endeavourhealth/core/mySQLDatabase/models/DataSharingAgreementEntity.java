package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDSA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "DataSharingAgreement", schema = "OrganisationManager")
public class DataSharingAgreementEntity {
    private String uuid;
    private String name;
    private String description;
    private String derivation;
    private short dsaStatusId;
    private short consentModelId;
    private Date startDate;
    private Date endDate;

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
    @Column(name = "Derivation", nullable = true, length = 100)
    public String getDerivation() {
        return derivation;
    }

    public void setDerivation(String derivation) {
        this.derivation = derivation;
    }

    @Basic
    @Column(name = "DSAStatusId", nullable = false)
    public short getDsaStatusId() {
        return dsaStatusId;
    }

    public void setDsaStatusId(short dsaStatusId) {
        this.dsaStatusId = dsaStatusId;
    }

    @Basic
    @Column(name = "ConsentModelId", nullable = false)
    public short getConsentModelId() {
        return consentModelId;
    }

    public void setConsentModelId(short consentModelId) {
        this.consentModelId = consentModelId;
    }

    @Basic
    @Column(name = "StartDate", nullable = true)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Basic
    @Column(name = "EndDate", nullable = true)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSharingAgreementEntity that = (DataSharingAgreementEntity) o;

        if (dsaStatusId != that.dsaStatusId) return false;
        if (consentModelId != that.consentModelId) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (derivation != null ? !derivation.equals(that.derivation) : that.derivation != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (derivation != null ? derivation.hashCode() : 0);
        result = 31 * result + (int) dsaStatusId;
        result = 31 * result + (int) consentModelId;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    public static List<DataSharingAgreementEntity> getAllDSAs() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
        Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);
        CriteriaQuery<DataSharingAgreementEntity> all = cq.select(rootEntry);
        TypedQuery<DataSharingAgreementEntity> allQuery = entityManager.createQuery(all);
        List<DataSharingAgreementEntity> ret = allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static DataSharingAgreementEntity getDSA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementEntity ret = entityManager.find(DataSharingAgreementEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementEntity dsaEntity = entityManager.find(DataSharingAgreementEntity.class, dsa.getUuid());
        entityManager.getTransaction().begin();
        dsaEntity.setName(dsa.getName());
        dsaEntity.setDescription(dsa.getDescription());
        dsaEntity.setDerivation(dsa.getDerivation());
        dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
        dsaEntity.setConsentModelId(dsa.getConsentModelId());
        if (dsa.getStartDate() != null) {
            dsaEntity.setStartDate(Date.valueOf(dsa.getStartDate()));
        }
        if (dsa.getEndDate() != null) {
            dsaEntity.setEndDate(Date.valueOf(dsa.getEndDate()));
        }
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementEntity dsaEntity = new DataSharingAgreementEntity();
        entityManager.getTransaction().begin();
        dsaEntity.setName(dsa.getName());
        dsaEntity.setDescription(dsa.getDescription());
        dsaEntity.setDerivation(dsa.getDerivation());
        dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
        dsaEntity.setConsentModelId(dsa.getConsentModelId());
        if (dsa.getStartDate() != null) {
            dsaEntity.setStartDate(Date.valueOf(dsa.getStartDate()));
        }
        if (dsa.getEndDate() != null) {
            dsaEntity.setEndDate(Date.valueOf(dsa.getEndDate()));
        }
        dsaEntity.setUuid(dsa.getUuid());
        entityManager.persist(dsaEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteDSA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataSharingAgreementEntity dsaEntity = entityManager.find(DataSharingAgreementEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dsaEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<DataSharingAgreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
        Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DataSharingAgreementEntity> query = entityManager.createQuery(cq);
        List<DataSharingAgreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<DataSharingAgreementEntity> getDSAsFromList(List<String> dsas) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSharingAgreementEntity> cq = cb.createQuery(DataSharingAgreementEntity.class);
        Root<DataSharingAgreementEntity> rootEntry = cq.from(DataSharingAgreementEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dsas);

        cq.where(predicate);
        TypedQuery<DataSharingAgreementEntity> query = entityManager.createQuery(cq);

        List<DataSharingAgreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
