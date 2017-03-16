package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDataFlow;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dataflow", schema = "organisationmanager")
public class DataflowEntity {
    private String uuid;
    private String name;
    private String status;
    private short flowScheduleId;
    private Integer approximateVolume;
    private short dataExchangeMethodId;
    private short flowStatusId;
    private String additionalDocumentation;
    private String signOff;
    private String dataSet;
    private String cohort;
    private String subscriber;
    private Short directionId;

    public static List<DataflowEntity> getAllDataFlows() throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataflowEntity> cq = cb.createQuery(DataflowEntity.class);
        Root<DataflowEntity> rootEntry = cq.from(DataflowEntity.class);
        CriteriaQuery<DataflowEntity> all = cq.select(rootEntry);
        TypedQuery<DataflowEntity> allQuery = entityManager.createQuery(all);
        return allQuery.getResultList();
    }

    public static DataflowEntity getDataFlow(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        return entityManager.find(DataflowEntity.class, uuid);
    }

    public static void updateDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DataflowEntity dataflowEntity = entityManager.find(DataflowEntity.class, dataFlow.getUuid());
        entityManager.getTransaction().begin();
        dataflowEntity.setName(dataFlow.getName());
        dataflowEntity.setStatus(dataFlow.getStatus());
        dataflowEntity.setDirectionId(dataFlow.getDirectionId());
        dataflowEntity.setFlowScheduleId(dataFlow.getFlowScheduleId());
        dataflowEntity.setApproximateVolume(dataFlow.getApproximateVolume());
        dataflowEntity.setDataExchangeMethodId(dataFlow.getDataExchangeMethodId());
        dataflowEntity.setFlowStatusId(dataFlow.getFlowStatusId());
        dataflowEntity.setAdditionalDocumentation(dataFlow.getAdditionalDocumentation());
        dataflowEntity.setSignOff(dataFlow.getSignOff());
        dataflowEntity.setDataSet(dataFlow.getDataSet());
        dataflowEntity.setCohort(dataFlow.getCohort());
        dataflowEntity.setSubscriber(dataFlow.getSubscriber());
        entityManager.getTransaction().commit();
    }

    public static void saveDataFlow(JsonDataFlow dataFlow) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DataflowEntity dataflowEntity = new DataflowEntity();
        entityManager.getTransaction().begin();
        dataflowEntity.setName(dataFlow.getName());
        dataflowEntity.setStatus(dataFlow.getStatus());
        dataflowEntity.setDirectionId(dataFlow.getDirectionId());
        dataflowEntity.setFlowScheduleId(dataFlow.getFlowScheduleId());
        dataflowEntity.setApproximateVolume(dataFlow.getApproximateVolume());
        dataflowEntity.setDataExchangeMethodId(dataFlow.getDataExchangeMethodId());
        dataflowEntity.setFlowStatusId(dataFlow.getFlowStatusId());
        dataflowEntity.setAdditionalDocumentation(dataFlow.getAdditionalDocumentation());
        dataflowEntity.setSignOff(dataFlow.getSignOff());
        dataflowEntity.setDataSet(dataFlow.getDataSet());
        dataflowEntity.setCohort(dataFlow.getCohort());
        dataflowEntity.setSubscriber(dataFlow.getSubscriber());
        dataflowEntity.setUuid(UUID.randomUUID().toString());
        entityManager.persist(dataflowEntity);
        entityManager.getTransaction().commit();
    }

    public static void deleteDataFlow(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        DataflowEntity dataflowEntity = entityManager.find(DataflowEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(dataflowEntity);
        entityManager.getTransaction().commit();
    }

    public static List<DataflowEntity> search(String expression) throws Exception {
        EntityManager entityManager = PersistenceManager.INSTANCE.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataflowEntity> cq = cb.createQuery(DataflowEntity.class);
        Root<DataflowEntity> rootEntry = cq.from(DataflowEntity.class);

        Predicate predicate = cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("status")), "%" + expression.toUpperCase() + "%"));

        cq.where(predicate);
        TypedQuery<DataflowEntity> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

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
    @Column(name = "Status", nullable = true, length = 100)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    @Column(name = "ApproximateVolume", nullable = true)
    public Integer getApproximateVolume() {
        return approximateVolume;
    }

    public void setApproximateVolume(Integer approximateVolume) {
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
    @Column(name = "DataSet", nullable = true, length = 36)
    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    @Basic
    @Column(name = "Cohort", nullable = true, length = 36)
    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }

    @Basic
    @Column(name = "Subscriber", nullable = true, length = 100)
    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataflowEntity that = (DataflowEntity) o;

        if (flowScheduleId != that.flowScheduleId) return false;
        if (dataExchangeMethodId != that.dataExchangeMethodId) return false;
        if (flowStatusId != that.flowStatusId) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (approximateVolume != null ? !approximateVolume.equals(that.approximateVolume) : that.approximateVolume != null)
            return false;
        if (additionalDocumentation != null ? !additionalDocumentation.equals(that.additionalDocumentation) : that.additionalDocumentation != null)
            return false;
        if (signOff != null ? !signOff.equals(that.signOff) : that.signOff != null) return false;
        if (dataSet != null ? !dataSet.equals(that.dataSet) : that.dataSet != null) return false;
        if (cohort != null ? !cohort.equals(that.cohort) : that.cohort != null) return false;
        if (subscriber != null ? !subscriber.equals(that.subscriber) : that.subscriber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) flowScheduleId;
        result = 31 * result + (approximateVolume != null ? approximateVolume.hashCode() : 0);
        result = 31 * result + (int) dataExchangeMethodId;
        result = 31 * result + (int) flowStatusId;
        result = 31 * result + (additionalDocumentation != null ? additionalDocumentation.hashCode() : 0);
        result = 31 * result + (signOff != null ? signOff.hashCode() : 0);
        result = 31 * result + (dataSet != null ? dataSet.hashCode() : 0);
        result = 31 * result + (cohort != null ? cohort.hashCode() : 0);
        result = 31 * result + (subscriber != null ? subscriber.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "DirectionId", nullable = true)
    public Short getDirectionId() {
        return directionId;
    }

    public void setDirectionId(Short directionId) {
        this.directionId = directionId;
    }
}
