package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.hl7.fhir.instance.model.Enumerations;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "patient_identifier_by_patient_id")
public class PatientIdentifierByPatientId {

    @ClusteringColumn(0)
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(1)
    @Column(name = "system_id")
    private UUID systemId = null;
    @Column(name = "nhs_number")
    private String nhsNumber = null;
    @Column(name = "forenames")
    private String forenames = null;
    @Column(name = "surname")
    private String surname = null;
    @Column(name = "date_of_birth")
    private Date dateOfBirth = null;
    @Column(name = "postcode")
    private String postcode = null;
    @Column(name = "gender")
    private Enumerations.AdministrativeGender gender = null;
    @PartitionKey
    @Column(name = "patient_id")
    private UUID patientId = null;
    @Column(name = "timestamp")
    private Date timestamp = null;
    @ClusteringColumn(4)
    @Column(name = "version")
    private UUID version = null;
    @ClusteringColumn(2)
    @Column(name = "local_id")
    private String localId = null;
    @ClusteringColumn(3)
    @Column(name = "local_id_system")
    private String localIdSystem = null;


    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public String getForenames() {
        return forenames;
    }

    public void setForenames(String forenames) {
        this.forenames = forenames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Enumerations.AdministrativeGender getGender() {
        return gender;
    }

    public void setGender(Enumerations.AdministrativeGender gender) {
        this.gender = gender;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getLocalIdSystem() {
        return localIdSystem;
    }

    public void setLocalIdSystem(String localIdSystem) {
        this.localIdSystem = localIdSystem;
    }

}
