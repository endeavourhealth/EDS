package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonDocumentation;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "Documentation", schema = "OrganisationManager")
public class DocumentationEntity {
    private String uuid;
    private String title;
    private String fileName;
    private byte[] fileData;

    @Id
    @Column(name = "Uuid", nullable = false, length = 36)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    @Column(name = "FileName", nullable = false, length = 50)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Basic
    @Column(name = "FileData", nullable = false)
    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentationEntity that = (DocumentationEntity) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        if (!Arrays.equals(fileData, that.fileData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(fileData);
        return result;
    }

    public static DocumentationEntity getDocument(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DocumentationEntity ret = entityManager.find(DocumentationEntity.class, uuid);
        entityManager.close();

        return ret;
    }

    public static void saveDocument(JsonDocumentation document) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DocumentationEntity documentationEntity = new DocumentationEntity();
        documentationEntity.setUuid(document.getUuid());
        documentationEntity.setFileName(document.getFilename());
        documentationEntity.setTitle(document.getTitle());
        documentationEntity.setFileData(document.getFileData());
        entityManager.getTransaction().begin();
        entityManager.persist(documentationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void updateDocument(JsonDocumentation document) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DocumentationEntity documentationEntity = entityManager.find(DocumentationEntity.class, document.getUuid());
        entityManager.getTransaction().begin();
        documentationEntity.setTitle(document.getTitle());
        documentationEntity.setFileName(document.getFilename());
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteDocument(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        DocumentationEntity documentationEntity = entityManager.find(DocumentationEntity.class, uuid);
        entityManager.getTransaction().begin();
        entityManager.remove(documentationEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void deleteAllAssociatedDocuments(String parentUuid, Short parentMapType) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        List<String> documents = MasterMappingEntity.getChildMappings(parentUuid, parentMapType, MapType.DOCUMENT.getMapType());

        if (documents.size() == 0)
            return;

        entityManager.getTransaction().begin();
        CriteriaBuilder criteriaBuilder  = entityManager.getCriteriaBuilder();
        CriteriaDelete<DocumentationEntity> query = criteriaBuilder.createCriteriaDelete(DocumentationEntity.class);
        Root<DocumentationEntity> root = query.from(DocumentationEntity.class);
        query.where(root.get("uuid").in(documents));

        entityManager.createQuery(query).executeUpdate();
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<DocumentationEntity> getDocumentsFromList(List<String> documents) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DocumentationEntity> cq = cb.createQuery(DocumentationEntity.class);
        Root<DocumentationEntity> rootEntry = cq.from(DocumentationEntity.class);

        Predicate predicate = rootEntry.get("uuid").in(documents);

        cq.where(predicate);
        TypedQuery<DocumentationEntity> query = entityManager.createQuery(cq);

        List<DocumentationEntity> ret = query.getResultList();

        entityManager.close();

        return ret;
    }
}
