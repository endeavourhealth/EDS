package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDataFlow;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
@Table(name = "DataFlow", schema = "OrganisationManager")
public class DataFlowEntity {
    private String uuid;
    private String name;
    private short flowScheduleId;
    private int approximateVolume;
    private short dataExchangeMethodId;
    private short flowStatusId;
    private String additionalDocumentation;
    private String signOff;
    private Short directionId;
    private short storageProtocolId;
    private short securityInfrastructureId;
    private short securityArchitectureId;

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
    @Column(name = "FlowScheduleId", nullable = false)
    public short getFlowScheduleId() {
        return flowScheduleId;
    }

    public void setFlowScheduleId(short flowScheduleId) {
        this.flowScheduleId = flowScheduleId;
    }

    @Basic
    @Column(name = "ApproximateVolume", nullable = false)
    public int getApproximateVolume() {
        return approximateVolume;
    }

    public void setApproximateVolume(int approximateVolume) {
        this.approximateVolume = approximateVolume;
    }

    @Basic
    @Column(name = "DataExchangeMethodId", nullable = false)
    public short getDataExchangeMethodId() {
        return dataExchangeMethodId;
    }

    public void setDataExchangeMethodId(short dataExchangeMethodId) {
        this.dataExchangeMethodId = dataExchangeMethodId;
    }

    @Basic
    @Column(name = "FlowStatusId", nullable = false)
    public short getFlowStatusId() {
        return flowStatusId;
    }

    public void setFlowStatusId(short flowStatusId) {
        this.flowStatusId = flowStatusId;
    }

    @Basic
    @Column(name = "AdditionalDocumentation", nullable = true, length = 100)
    public String getAdditionalDocumentation() {
        return additionalDocumentation;
    }

    public void setAdditionalDocumentation(String additionalDocumentation) {
        this.additionalDocumentation = additionalDocumentation;
    }

    @Basic
    @Column(name = "SignOff", nullable = true, length = 10)
    public String getSignOff() {
        return signOff;
    }

    public void setSignOff(String signOff) {
        this.signOff = signOff;
    }

    @Basic
    @Column(name = "DirectionId", nullable = true)
    public Short getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Short directionId) {
        this.directionId = directionId;
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
    @Column(name = "SecurityInfrastructureId", nullable = false)
    public short getSecurityInfrastructureId() {
        return securityInfrastructureId;
    }

    public void setSecurityInfrastructureId(short securityInfrastructureId) {
        this.securityInfrastructureId = securityInfrastructureId;
    }

    @Basic
    @Column(name = "SecurityArchitectureId", nullable = false)
    public short getSecurityArchitectureId() {
        return securityArchitectureId;
    }

    public void setSecurityArchitectureId(short securityArchitectureId) {
        this.securityArchitectureId = securityArchitectureId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataFlowEntity that = (DataFlowEntity) o;

        if (flowScheduleId != that.flowScheduleId) return false;
        if (approximateVolume != that.approximateVolume) return false;
        if (dataExchangeMethodId != that.dataExchangeMethodId) return false;
        if (flowStatusId != that.flowStatusId) return false;
        if (storageProtocolId != that.storageProtocolId) return false;
        if (securityInfrastructureId != that.securityInfrastructureId) return false;
        if (securityArchitectureId != that.securityArchitectureId) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (additionalDocumentation != null ? !additionalDocumentation.equals(that.additionalDocumentation) : that.additionalDocumentation != null)
            return false;
        if (signOff != null ? !signOff.equals(that.signOff) : that.signOff != null) return false;
        if (directionId != null ? !directionId.equals(that.directionId) : that.directionId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) flowScheduleId;
        result = 31 * result + approximateVolume;
        result = 31 * result + (int) dataExchangeMethodId;
        result = 31 * result + (int) flowStatusId;
        result = 31 * result + (additionalDocumentation != null ? additionalDocumentation.hashCode() : 0);
        result = 31 * result + (signOff != null ? signOff.hashCode() : 0);
        result = 31 * result + (directionId != null ? directionId.hashCode() : 0);
        result = 31 * result + (int) storageProtocolId;
        result = 31 * result + (int) securityInfrastructureId;
        result = 31 * result + (int) securityArchitectureId;
        return result;
    }

    public static List<DataFlowEntity> getAllDataFlows() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);
        CriteriaQuery<DataFlowEntity> all = cq.select(rootEntry);
        TypedQuery<DataFlowEntity> allQuery = entityManager.createQuery(all);
        List<DataFlowEntity> ret =  allQuery.getResultList();

        entityManager.close();

        return ret;
    }

    public static DataFlowEntity getDataFlow(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataFlowEntity ret = entityManager.find(DataFlowEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataFlowEntity dataFlowEntity = entityManager.find(DataFlowEntity.class, dataFlow.getUuid());
        entityManager.getTransaction().begin();
        dataFlowEntity.setName(dataFlow.getName());
        dataFlowEntity.setDirectionId(dataFlow.getDirectionId());
        dataFlowEntity.setFlowScheduleId(dataFlow.getFlowScheduleId());
        dataFlowEntity.setApproximateVolume(dataFlow.getApproximateVolume());
        dataFlowEntity.setDataExchangeMethodId(dataFlow.getDataExchangeMethodId());
        dataFlowEntity.setStorageProtocolId(dataFlow.getStorageProtocolId());
        dataFlowEntity.setSecurityInfrastructureId(dataFlow.getSecurityInfrastructureId());
        dataFlowEntity.setSecurityArchitectureId(dataFlow.getSecurityArchitectureId());
        dataFlowEntity.setFlowStatusId(dataFlow.getFlowStatusId());
        dataFlowEntity.setAdditionalDocumentation(dataFlow.getAdditionalDocumentation());
        dataFlowEntity.setSignOff(dataFlow.getSignOff());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataFlowEntity dataFlowEntity = new DataFlowEntity();
        entityManager.getTransaction().begin();
        dataFlowEntity.setName(dataFlow.getName());
        dataFlowEntity.setDirectionId(dataFlow.getDirectionId());
        dataFlowEntity.setFlowScheduleId(dataFlow.getFlowScheduleId());
        dataFlowEntity.setApproximateVolume(dataFlow.getApproximateVolume());
        dataFlowEntity.setDataExchangeMethodId(dataFlow.getDataExchangeMethodId());
        dataFlowEntity.setFlowStatusId(dataFlow.getFlowStatusId());
        dataFlowEntity.setAdditionalDocumentation(dataFlow.getAdditionalDocumentation());
        dataFlowEntity.setSignOff(dataFlow.getSignOff());
        dataFlowEntity.setUuid(dataFlow.getUuid());
        entityManager.persist(dataFlowEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteDataFlow(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DataFlowEntity dataFlowEntity = entityManager.find(DataFlowEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dataFlowEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<DataFlowEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("status")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DataFlowEntity> query = entityManager.createQuery(cq);
        List<DataFlowEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<DataFlowEntity> getDataFlowsFromList(List<String> dataFlows) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataFlowEntity> cq = cb.createQuery(DataFlowEntity.class);
        Root<DataFlowEntity> rootEntry = cq.from(DataFlowEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(dataFlows);

        cq.where(predicate);
        TypedQuery<DataFlowEntity> query = entityManager.createQuery(cq);

        List<DataFlowEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
