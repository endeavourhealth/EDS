package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDSA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "datasharingagreement", schema = "organisationmanager")
public class DatasharingagreementEntity {
    private String uuid;
    private String name;
    private String description;
    private String derivation;
    private String publisherInformation;
    private String subscriberInformation;
    private String publisherContractInformation;
    private String subscriberContractInformation;
    private short dsaStatusId;
    private String consentModel;

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
    @Column(name = "PublisherInformation", nullable = true, length = 100)
    public String getPublisherInformation() {
        return publisherInformation;
    }

    public void setPublisherInformation(String publisherInformation) {
        this.publisherInformation = publisherInformation;
    }

    @Basic
    @Column(name = "SubscriberInformation", nullable = true, length = 100)
    public String getSubscriberInformation() {
        return subscriberInformation;
    }

    public void setSubscriberInformation(String subscriberInformation) {
        this.subscriberInformation = subscriberInformation;
    }

    @Basic
    @Column(name = "PublisherContractInformation", nullable = true, length = 100)
    public String getPublisherContractInformation() {
        return publisherContractInformation;
    }

    public void setPublisherContractInformation(String publisherContractInformation) {
        this.publisherContractInformation = publisherContractInformation;
    }

    @Basic
    @Column(name = "SubscriberContractInformation", nullable = true, length = 100)
    public String getSubscriberContractInformation() {
        return subscriberContractInformation;
    }

    public void setSubscriberContractInformation(String subscriberContractInformation) {
        this.subscriberContractInformation = subscriberContractInformation;
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
    @Column(name = "ConsentModel", nullable = true, length = 36)
    public String getConsentModel() {
        return consentModel;
    }

    public void setConsentModel(String consentModel) {
        this.consentModel = consentModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatasharingagreementEntity that = (DatasharingagreementEntity) o;

        if (dsaStatusId != that.dsaStatusId) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (derivation != null ? !derivation.equals(that.derivation) : that.derivation != null) return false;
        if (publisherInformation != null ? !publisherInformation.equals(that.publisherInformation) : that.publisherInformation != null)
            return false;
        if (subscriberInformation != null ? !subscriberInformation.equals(that.subscriberInformation) : that.subscriberInformation != null)
            return false;
        if (publisherContractInformation != null ? !publisherContractInformation.equals(that.publisherContractInformation) : that.publisherContractInformation != null)
            return false;
        if (subscriberContractInformation != null ? !subscriberContractInformation.equals(that.subscriberContractInformation) : that.subscriberContractInformation != null)
            return false;
        if (consentModel != null ? !consentModel.equals(that.consentModel) : that.consentModel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (derivation != null ? derivation.hashCode() : 0);
        result = 31 * result + (publisherInformation != null ? publisherInformation.hashCode() : 0);
        result = 31 * result + (subscriberInformation != null ? subscriberInformation.hashCode() : 0);
        result = 31 * result + (publisherContractInformation != null ? publisherContractInformation.hashCode() : 0);
        result = 31 * result + (subscriberContractInformation != null ? subscriberContractInformation.hashCode() : 0);
        result = 31 * result + (int) dsaStatusId;
        result = 31 * result + (consentModel != null ? consentModel.hashCode() : 0);
        return result;
    }

    public static List<DatasharingagreementEntity> getAllDSAs() throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasharingagreementEntity> cq = cb.createQuery(DatasharingagreementEntity.class);
        Root<DatasharingagreementEntity> rootEntry = cq.from(DatasharingagreementEntity.class);
        CriteriaQuery<DatasharingagreementEntity> all = cq.select(rootEntry);
        TypedQuery<DatasharingagreementEntity> allQuery = entityManager.createQuery(all);
        return allQuery.getResultList();
    }

    public static DatasharingagreementEntity getDSA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        return entityManager.find(DatasharingagreementEntity.class, uuid);
    }

    public static void updateDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DatasharingagreementEntity dsaEntity = entityManager.find(DatasharingagreementEntity.class, dsa.getUuid());
        entityManager.getTransaction().begin();
        dsaEntity.setName(dsa.getName());
        dsaEntity.setDescription(dsa.getDescription());
        dsaEntity.setDerivation(dsa.getDerivation());
        dsaEntity.setPublisherInformation(dsa.getPublisherInformation());
        dsaEntity.setPublisherContractInformation(dsa.getPublisherContractInformation());
        dsaEntity.setSubscriberInformation(dsa.getSubscriberInformation());
        dsaEntity.setSubscriberContractInformation(dsa.getSubscriberContractInformation());
        dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
        dsaEntity.setConsentModel(dsa.getConsentModel());
        entityManager.getTransaction().commit();
    }

    public static void saveDSA(JsonDSA dsa) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DatasharingagreementEntity dsaEntity = new DatasharingagreementEntity();
        entityManager.getTransaction().begin();
        dsaEntity.setName(dsa.getName());
        dsaEntity.setDescription(dsa.getDescription());
        dsaEntity.setDerivation(dsa.getDerivation());
        dsaEntity.setPublisherInformation(dsa.getPublisherInformation());
        dsaEntity.setPublisherContractInformation(dsa.getPublisherContractInformation());
        dsaEntity.setSubscriberInformation(dsa.getSubscriberInformation());
        dsaEntity.setSubscriberContractInformation(dsa.getSubscriberContractInformation());
        dsaEntity.setDsaStatusId(dsa.getDsaStatusId());
        dsaEntity.setConsentModel(dsa.getConsentModel());
        dsaEntity.setUuid(dsa.getUuid());
        entityManager.persist(dsaEntity);
        entityManager.getTransaction().commit();
    }

    public static void deleteDSA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DatasharingagreementEntity dsaEntity = entityManager.find(DatasharingagreementEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dsaEntity);
        entityManager.getTransaction().commit();
    }

    public static List<DatasharingagreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasharingagreementEntity> cq = cb.createQuery(DatasharingagreementEntity.class);
        Root<DatasharingagreementEntity> rootEntry = cq.from(DatasharingagreementEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DatasharingagreementEntity> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public static List<DatasharingagreementEntity> getDSAsFromList(List<String> dsas) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasharingagreementEntity> cq = cb.createQuery(DatasharingagreementEntity.class);
        Root<DatasharingagreementEntity> rootEntry = cq.from(DatasharingagreementEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dsas);

        cq.where(predicate);
        TypedQuery<DatasharingagreementEntity> query = entityManager.createQuery(cq);

        return query.getResultList();
    }
}
