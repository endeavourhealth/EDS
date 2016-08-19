package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "patient_identifier_by_nhs_number")
public class PatientIdentifierByNhsNumber {

    @ClusteringColumn(1)
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(2)
    @Column(name = "system_id")
    private UUID systemId = null;
    @Column(name = "local_id")
    private String localId = null;
    @ClusteringColumn(0)
    @Column(name = "nhs_number")
    private String nhsNumber = null;
    @ClusteringColumn(3)
    @Column(name = "patient_id")
    private UUID patientId = null;
    @Column(name = "timestamp")
    private Date timestamp = null;
    @ClusteringColumn(4)
    @Column(name = "version")
    private UUID version = null;


/*

    json.setForenames(identifier.getForenames());
    json.setSurname(identifier.getSurname());
    json.setDateOfBirth(identifier.getDateOfBirth());
    json.setPostcode(identifier.getPostcode());
    json.setGender(identifier.getGender());
    json.setLocalIdSystem(identifier.getLocalIdSystem());

*/



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

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
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
}
