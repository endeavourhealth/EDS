package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dataprocessingagreement", schema = "organisationmanager")
public class DataprocessingagreementEntity {
    private String uuid;
    private String name;
    private String description;
    private String derivation;
    private String publisherInformation;
    private String publisherContractInformation;
    private String publisherDataSet;
    private short dsaStatusId;
    private short storageProtocolId;
    private String dataFlow;
    private String returnToSenderPolicy;

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
    @Column(name = "PublisherContractInformation", nullable = true, length = 100)
    public String getPublisherContractInformation() {
        return publisherContractInformation;
    }

    public void setPublisherContractInformation(String publisherContractInformation) {
        this.publisherContractInformation = publisherContractInformation;
    }

    @Basic
    @Column(name = "PublisherDataSet", nullable = true, length = 36)
    public String getPublisherDataSet() {
        return publisherDataSet;
    }

    public void setPublisherDataSet(String publisherDataSet) {
        this.publisherDataSet = publisherDataSet;
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
    @Column(name = "StorageProtocolId", nullable = false)
    public short getStorageProtocolId() {
        return storageProtocolId;
    }

    public void setStorageProtocolId(short storageProtocolId) {
        this.storageProtocolId = storageProtocolId;
    }

    @Basic
    @Column(name = "DataFlow", nullable = true, length = 36)
    public String getDataFlow() {
        return dataFlow;
    }

    public void setDataFlow(String dataFlow) {
        this.dataFlow = dataFlow;
    }

    @Basic
    @Column(name = "ReturnToSenderPolicy", nullable = true, length = 100)
    public String getReturnToSenderPolicy() {
        return returnToSenderPolicy;
    }

    public void setReturnToSenderPolicy(String returnToSenderPolicy) {
        this.returnToSenderPolicy = returnToSenderPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataprocessingagreementEntity that = (DataprocessingagreementEntity) o;

        if (dsaStatusId != that.dsaStatusId) return false;
        if (storageProtocolId != that.storageProtocolId) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (derivation != null ? !derivation.equals(that.derivation) : that.derivation != null) return false;
        if (publisherInformation != null ? !publisherInformation.equals(that.publisherInformation) : that.publisherInformation != null)
            return false;
        if (publisherContractInformation != null ? !publisherContractInformation.equals(that.publisherContractInformation) : that.publisherContractInformation != null)
            return false;
        if (publisherDataSet != null ? !publisherDataSet.equals(that.publisherDataSet) : that.publisherDataSet != null)
            return false;
        if (dataFlow != null ? !dataFlow.equals(that.dataFlow) : that.dataFlow != null) return false;
        if (returnToSenderPolicy != null ? !returnToSenderPolicy.equals(that.returnToSenderPolicy) : that.returnToSenderPolicy != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (derivation != null ? derivation.hashCode() : 0);
        result = 31 * result + (publisherInformation != null ? publisherInformation.hashCode() : 0);
        result = 31 * result + (publisherContractInformation != null ? publisherContractInformation.hashCode() : 0);
        result = 31 * result + (publisherDataSet != null ? publisherDataSet.hashCode() : 0);
        result = 31 * result + (int) dsaStatusId;
        result = 31 * result + (int) storageProtocolId;
        result = 31 * result + (dataFlow != null ? dataFlow.hashCode() : 0);
        result = 31 * result + (returnToSenderPolicy != null ? returnToSenderPolicy.hashCode() : 0);
        return result;
    }

    public static List<DataprocessingagreementEntity> getAllDPAs() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataprocessingagreementEntity> cq = cb.createQuery(DataprocessingagreementEntity.class);
        Root<DataprocessingagreementEntity> rootEntry = cq.from(DataprocessingagreementEntity.class);
        CriteriaQuery<DataprocessingagreementEntity> all = cq.select(rootEntry);
        TypedQuery<DataprocessingagreementEntity> allQuery = entityManager.createQuery(all);
        List<DataprocessingagreementEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static DataprocessingagreementEntity getDPA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataprocessingagreementEntity ret = entityManager.find(DataprocessingagreementEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataprocessingagreementEntity dpaEntity = entityManager.find(DataprocessingagreementEntity.class, dpa.getUuid());
        entityManager.getTransaction().begin();
        dpaEntity.setName(dpa.getName());
        dpaEntity.setDescription(dpa.getDescription());
        dpaEntity.setDerivation(dpa.getDerivation());
        dpaEntity.setPublisherInformation(dpa.getPublisherInformation());
        dpaEntity.setPublisherContractInformation(dpa.getPublisherContractInformation());
        dpaEntity.setPublisherDataSet(dpa.getPublisherDataSet());
        dpaEntity.setDsaStatusId(dpa.getDsaStatusId());
        dpaEntity.setStorageProtocolId(dpa.getStorageProtocolId());
        dpaEntity.setDataFlow(dpa.getDataFlow());
        dpaEntity.setReturnToSenderPolicy(dpa.getReturnToSenderPolicy());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveDPA(JsonDPA dpa) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataprocessingagreementEntity dpaEntity = new DataprocessingagreementEntity();
        entityManager.getTransaction().begin();
        dpaEntity.setName(dpa.getName());
        dpaEntity.setName(dpa.getName());
        dpaEntity.setDescription(dpa.getDescription());
        dpaEntity.setDerivation(dpa.getDerivation());
        dpaEntity.setPublisherInformation(dpa.getPublisherInformation());
        dpaEntity.setPublisherContractInformation(dpa.getPublisherContractInformation());
        dpaEntity.setPublisherDataSet(dpa.getPublisherDataSet());
        dpaEntity.setDsaStatusId(dpa.getDsaStatusId());
        dpaEntity.setStorageProtocolId(dpa.getStorageProtocolId());
        dpaEntity.setDataFlow(dpa.getDataFlow());
        dpaEntity.setReturnToSenderPolicy(dpa.getReturnToSenderPolicy());
        dpaEntity.setUuid(dpa.getUuid());
        entityManager.persist(dpaEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteDPA(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataprocessingagreementEntity dpaEntity = entityManager.find(DataprocessingagreementEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dpaEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<DataprocessingagreementEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataprocessingagreementEntity> cq = cb.createQuery(DataprocessingagreementEntity.class);
        Root<DataprocessingagreementEntity> rootEntry = cq.from(DataprocessingagreementEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("description")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DataprocessingagreementEntity> query = entityManager.createQuery(cq);
        List<DataprocessingagreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<DataprocessingagreementEntity> getDPAsFromList(List<String> dpas) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataprocessingagreementEntity> cq = cb.createQuery(DataprocessingagreementEntity.class);
        Root<DataprocessingagreementEntity> rootEntry = cq.from(DataprocessingagreementEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dpas);

        cq.where(predicate);
        TypedQuery<DataprocessingagreementEntity> query = entityManager.createQuery(cq);

        List<DataprocessingagreementEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
