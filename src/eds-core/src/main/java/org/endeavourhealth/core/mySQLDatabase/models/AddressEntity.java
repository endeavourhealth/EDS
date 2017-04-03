package org.endeavourhealth.core.mySQLDatabase.models;

import org.endeavourhealth.core.mySQLDatabase.PersistenceManager;
import org.endeavourhealth.coreui.json.JsonAddress;
import org.hl7.fhir.instance.model.Address;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "getOrganisationMarkers",
                procedureName = "getOrganisationMarkers",
                parameters = {
                        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "RegionId")
                }
        )
})
@Entity
@Table(name = "Address", schema = "OrganisationManager")
public class AddressEntity {
    private String uuid;
    private String organisationUuid;
    private String buildingName;
    private String numberAndStreet;
    private String locality;
    private String city;
    private String county;
    private String postcode;
    private String geolocation;
    private Byte geolocationReprocess;
    private Double lat;
    private Double lng;

    public AddressEntity() {

    }

    public AddressEntity(JsonAddress address) {
        this.uuid = address.getUuid();
        this.organisationUuid = address.getOrganisationUuid();
        this.buildingName = address.getBuildingName();
        this.numberAndStreet = address.getNumberAndStreet();
        this.locality = address.getLocality();
        this.city = address.getCity();
        this.county = address.getCounty();
        this.postcode = address.getPostcode();
        this.lat = address.getLat();
        this.lng = address.getLng();
        this.geolocationReprocess = 0;
    }

    public static List<AddressEntity> getAddressesForOrganisation(String uuid) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);
        Root<AddressEntity> rootEntry = cq.from(AddressEntity.class);

        Predicate predicate = cb.equal(cb.upper(rootEntry.get("organisationUuid")), uuid.toUpperCase());
        cq.where(predicate);

        TypedQuery<AddressEntity> query = entityManager.createQuery(cq);
        List<AddressEntity> ret = query.getResultList();
        entityManager.close();
        return ret;
    }

    public static void bulkSaveAddresses(List<AddressEntity> addressEntities) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        int batchSize = 50;

        entityManager.getTransaction().begin();

        for (int i = 0; i < addressEntities.size(); i++) {
            AddressEntity addressEntity = addressEntities.get(i);
            entityManager.persist(addressEntity);
            if (i % batchSize == 0){
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void saveAddress(JsonAddress address) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        AddressEntity addressEntity = new AddressEntity(address);
        addressEntity.setUuid(address.getUuid());
        entityManager.getTransaction().begin();
        entityManager.persist(addressEntity);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void updateAddress(JsonAddress address) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        AddressEntity addressEntity = entityManager.find(AddressEntity.class, address.getUuid());
        entityManager.getTransaction().begin();
        addressEntity.setOrganisationUuid(address.getOrganisationUuid());
        addressEntity.setBuildingName(address.getBuildingName());
        addressEntity.setNumberAndStreet(address.getNumberAndStreet());
        addressEntity.setLocality(address.getLocality());
        addressEntity.setCity(address.getCity());
        addressEntity.setCounty(address.getCounty());
        addressEntity.setPostcode(address.getPostcode());
        addressEntity.setGeolocationReprocess((byte)0);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static void updateGeolocation(JsonAddress address) throws Exception {
        EntityManager entityManager = PersistenceManager.getEntityManager();

        AddressEntity addressEntity = entityManager.find(AddressEntity.class, address.getUuid());
        entityManager.getTransaction().begin();
        addressEntity.setLat(address.getLat());
        addressEntity.setLng(address.getLng());
        addressEntity.setGeolocationReprocess((byte)0);
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public static List<Object[]> getOrganisationsMarkers(String regionUUID) throws Exception {

        EntityManager entityManager = PersistenceManager.getEntityManager();

        StoredProcedureQuery spq = entityManager.createNamedStoredProcedureQuery("getOrganisationMarkers");
        spq.setParameter("RegionId", regionUUID);
        spq.execute();
        List<Object[]> ent = spq.getResultList();

        entityManager.close();

        return ent;
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
    @Column(name = "OrganisationUuid", nullable = false, length = 36)
    public String getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(String organisationUuid) {
        this.organisationUuid = organisationUuid;
    }

    @Basic
    @Column(name = "BuildingName", nullable = true, length = 100)
    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    @Basic
    @Column(name = "NumberAndStreet", nullable = true, length = 100)
    public String getNumberAndStreet() {
        return numberAndStreet;
    }

    public void setNumberAndStreet(String numberAndStreet) {
        this.numberAndStreet = numberAndStreet;
    }

    @Basic
    @Column(name = "Locality", nullable = true, length = 100)
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Basic
    @Column(name = "City", nullable = true, length = 100)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @Column(name = "County", nullable = true, length = 100)
    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Basic
    @Column(name = "Postcode", nullable = true, length = 100)
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Basic
    @Column(name = "GeolocationReprocess", nullable = true)
    public Byte getGeolocationReprocess() {
        return geolocationReprocess;
    }

    public void setGeolocationReprocess(Byte geolocationReprocess) {
        this.geolocationReprocess = geolocationReprocess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddressEntity that = (AddressEntity) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (organisationUuid != null ? !organisationUuid.equals(that.organisationUuid) : that.organisationUuid != null)
            return false;
        if (buildingName != null ? !buildingName.equals(that.buildingName) : that.buildingName != null) return false;
        if (numberAndStreet != null ? !numberAndStreet.equals(that.numberAndStreet) : that.numberAndStreet != null)
            return false;
        if (locality != null ? !locality.equals(that.locality) : that.locality != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (county != null ? !county.equals(that.county) : that.county != null) return false;
        if (postcode != null ? !postcode.equals(that.postcode) : that.postcode != null) return false;
        if (geolocation != null ? !geolocation.equals(that.geolocation) : that.geolocation != null) return false;
        if (geolocationReprocess != null ? !geolocationReprocess.equals(that.geolocationReprocess) : that.geolocationReprocess != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (organisationUuid != null ? organisationUuid.hashCode() : 0);
        result = 31 * result + (buildingName != null ? buildingName.hashCode() : 0);
        result = 31 * result + (numberAndStreet != null ? numberAndStreet.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (postcode != null ? postcode.hashCode() : 0);
        result = 31 * result + (geolocation != null ? geolocation.hashCode() : 0);
        result = 31 * result + (geolocationReprocess != null ? geolocationReprocess.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "lat", nullable = true, precision = 6)
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Basic
    @Column(name = "lng", nullable = true, precision = 6)
    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
