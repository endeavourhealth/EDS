package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonOrganisation;
import org.endeavourhealth.coreui.json.JsonOrganisationManager;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "deleteUneditedBulkOrganisations",
                procedureName = "deleteUneditedBulkOrganisations"
        ),
        @NamedStoredProcedureQuery(
                name = "getServiceStatistics",
                procedureName = "getServiceStatistics"
        ),
        @NamedStoredProcedureQuery(
                name = "getOrganisationStatistics",
                procedureName = "getOrganisationStatistics"
        ),
        @NamedStoredProcedureQuery(
                name = "getRegionStatistics",
                procedureName = "getRegionStatistics"
        )
})
@Table(name = "Organisation", schema = "OrganisationManager")
public class OrganisationEntity {

    private String name;
    private String alternativeName;
    private String odsCode;
    private String icoCode;
    private String igToolkitStatus;
    private Date dateOfRegistration;
    private Integer registrationPerson;
    private String evidenceOfRegistration;
    private String uuid;
    private byte isService;
    private byte bulkImported;
    private byte bulkItemUpdated;
    private String bulkConflictedWith;

    public static void deleteUneditedBulkOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("deleteUneditedBulkOrganisations");
        spq.execute();
        entityManager.close();
    }

    public static List<Object[]> getStatistics(String procName) throws Exception {

        EntityManager entityManager = PersistenceManager.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery(procName);
        spq.execute();
        List<Object[]> ent = spq.getResultList();
        entityManager.close();

        return ent;
    }

    public static List<OrganisationEntity> getOrganisationsFromList(List<String> organisations) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(organisations);

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);

        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<OrganisationEntity> getAllOrganisations(boolean services) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        //Services are just organisations with the isService flag set to true;
        Predicate predicate = cb.equal(rootEntry.get("isService"), (byte) (services ? 1 : 0));

        cq.where(predicate).orderBy(cb.asc(rootEntry.get("name")));
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);

        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static OrganisationEntity getOrganisation(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity ret = entityManager.find(OrganisationEntity.class, uuid);

        entityManager.close();

        return ret;
    }

    public static void updateOrganisation(JsonOrganisationManager organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity organisationEntity = entityManager.find(OrganisationEntity.class, organisation.getUuid());
        entityManager.getTransaction().begin();
        organisationEntity.setName(organisation.getName());
        organisationEntity.setAlternativeName(organisation.getAlternativeName());
        organisationEntity.setOdsCode(organisation.getOdsCode());
        organisationEntity.setIcoCode(organisation.getIcoCode());
        organisationEntity.setIgToolkitStatus(organisation.getIgToolkitStatus());
        organisationEntity.setIsService((byte) (organisation.getIsService().equals("1") ? 1 : 0));
        organisationEntity.setBulkItemUpdated((byte)1);
        if (organisation.getDateOfRegistration() != null) {
            organisationEntity.setDateOfRegistration(Date.valueOf(organisation.getDateOfRegistration()));
        }
        //organisationEntity.setRegistrationPerson(organisation.getRegistrationPerson());
        organisationEntity.setEvidenceOfRegistration(organisation.getEvidenceOfRegistration());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveOrganisation(JsonOrganisationManager organisation) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity organisationEntity = new OrganisationEntity();
        entityManager.getTransaction().begin();
        organisationEntity.setName(organisation.getName());
        organisationEntity.setAlternativeName(organisation.getAlternativeName());
        organisationEntity.setOdsCode(organisation.getOdsCode());
        organisationEntity.setIcoCode(organisation.getIcoCode());
        organisationEntity.setIgToolkitStatus(organisation.getIgToolkitStatus());
        organisationEntity.setIsService((byte) (organisation.getIsService().equals("1") ? 1 : 0));
        organisationEntity.setBulkImported((byte) (organisation.getBulkImported().equals("1") ? 1 : 0));
        organisationEntity.setBulkItemUpdated((byte) (organisation.getBulkItemUpdated().equals("1") ? 1 : 0));
        if (organisation.getDateOfRegistration() != null) {
            organisationEntity.setDateOfRegistration(Date.valueOf(organisation.getDateOfRegistration()));
        }
        //organisationEntity.setRegistrationPerson(organisation.getRegistrationPerson());
        organisationEntity.setEvidenceOfRegistration(organisation.getEvidenceOfRegistration());
        organisationEntity.setUuid(organisation.getUuid());
        entityManager.persist(organisationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void bulkSaveOrganisation(List<OrganisationEntity> organisationEntities) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        int batchSize = 50;

        entityManager.getTransaction().begin();

        for (int i = 0; i < organisationEntities.size(); i++) {
            OrganisationEntity organisationEntity = organisationEntities.get(i);
            entityManager.persist(organisationEntity);
            if (i % batchSize == 0){
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteOrganisation(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        OrganisationEntity organisationEntity = entityManager.find(OrganisationEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(organisationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<OrganisationEntity> search(String expression, boolean searchServices) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.and(cb.equal(rootEntry.get("isService"), (byte) (searchServices ? 1 : 0)), (cb.or(cb.like(cb.upper(rootEntry.get("name")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("odsCode")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("alternativeName")), "%" + expression.toUpperCase() + "%"),
                cb.like(cb.upper(rootEntry.get("icoCode")), "%" + expression.toUpperCase() + "%"))));

        cq.where(predicate).orderBy(cb.asc(rootEntry.get("name")));
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<OrganisationEntity> getUpdatedBulkOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.and(cb.equal(rootEntry.get("bulkImported"), (byte) 1),
            (cb.equal(rootEntry.get("bulkItemUpdated"), (byte) 1)));

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    public static List<OrganisationEntity> getConflictedOrganisations() throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrganisationEntity> cq = cb.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> rootEntry = cq.from(OrganisationEntity.class);

        Predicate predicate = cb.isNotNull(rootEntry.get("bulkConflictedWith"));

        cq.where(predicate);
        TypedQuery<OrganisationEntity> query = entityManager.createQuery(cq);
        List<OrganisationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "alternative_name", nullable = true, length = 100)
    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    @Basic
    @Column(name = "ods_code", nullable = true, length = 10)
    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }

    @Basic
    @Column(name = "ico_code", nullable = true, length = 10)
    public String getIcoCode() {
        return icoCode;
    }

    public void setIcoCode(String icoCode) {
        this.icoCode = icoCode;
    }

    @Basic
    @Column(name = "ig_toolkit_status", nullable = true, length = 10)
    public String getIgToolkitStatus() {
        return igToolkitStatus;
    }

    public void setIgToolkitStatus(String igToolkitStatus) {
        this.igToolkitStatus = igToolkitStatus;
    }

    @Basic
    @Column(name = "date_of_registration", nullable = false)
    public Date getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(Date dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    @Basic
    @Column(name = "registration_person", nullable = true)
    public Integer getRegistrationPerson() {
        return registrationPerson;
    }

    public void setRegistrationPerson(Integer registrationPerson) {
        this.registrationPerson = registrationPerson;
    }

    @Basic
    @Column(name = "evidence_of_registration", nullable = true, length = 500)
    public String getEvidenceOfRegistration() {
        return evidenceOfRegistration;
    }

    public void setEvidenceOfRegistration(String evidenceOfRegistration) {
        this.evidenceOfRegistration = evidenceOfRegistration;
    }

    @Id
    @Column(name = "uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(name = "IsService", nullable = false)
    public byte getIsService() {
        return isService;
    }

    public void setIsService(byte isService) {
        this.isService = isService;
    }

    @Basic
    @Column(name = "BulkImported", nullable = false)
    public byte getBulkImported() {
        return bulkImported;
    }

    public void setBulkImported(byte bulkImported) {
        this.bulkImported = bulkImported;
    }

    @Basic
    @Column(name = "BulkItemUpdated", nullable = false)
    public byte getBulkItemUpdated() {
        return bulkItemUpdated;
    }

    public void setBulkItemUpdated(byte bulkItemUpdated) {
        this.bulkItemUpdated = bulkItemUpdated;
    }

    @Basic
    @Column(name = "BulkConflictedWith", nullable = true, length = 36)
    public String getBulkConflictedWith() {
        return bulkConflictedWith;
    }

    public void setBulkConflictedWith(String bulkConflictedWith) {
        this.bulkConflictedWith = bulkConflictedWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganisationEntity that = (OrganisationEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (alternativeName != null ? !alternativeName.equals(that.alternativeName) : that.alternativeName != null)
            return false;
        if (odsCode != null ? !odsCode.equals(that.odsCode) : that.odsCode != null) return false;
        if (icoCode != null ? !icoCode.equals(that.icoCode) : that.icoCode != null) return false;
        if (igToolkitStatus != null ? !igToolkitStatus.equals(that.igToolkitStatus) : that.igToolkitStatus != null)
            return false;
        if (dateOfRegistration != null ? !dateOfRegistration.equals(that.dateOfRegistration) : that.dateOfRegistration != null)
            return false;
        if (registrationPerson != null ? !registrationPerson.equals(that.registrationPerson) : that.registrationPerson != null)
            return false;
        if (evidenceOfRegistration != null ? !evidenceOfRegistration.equals(that.evidenceOfRegistration) : that.evidenceOfRegistration != null)
            return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (alternativeName != null ? alternativeName.hashCode() : 0);
        result = 31 * result + (odsCode != null ? odsCode.hashCode() : 0);
        result = 31 * result + (icoCode != null ? icoCode.hashCode() : 0);
        result = 31 * result + (igToolkitStatus != null ? igToolkitStatus.hashCode() : 0);
        result = 31 * result + (dateOfRegistration != null ? dateOfRegistration.hashCode() : 0);
        result = 31 * result + (registrationPerson != null ? registrationPerson.hashCode() : 0);
        result = 31 * result + (evidenceOfRegistration != null ? evidenceOfRegistration.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }
}
