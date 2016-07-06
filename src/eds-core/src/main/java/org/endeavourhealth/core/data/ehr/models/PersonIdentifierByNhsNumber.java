package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "person_identifier_by_nhs_number")
public class PersonIdentifierByNhsNumber {

    @ClusteringColumn(1)
    @Column(name = "organisation_id")
    private UUID organisationId = null;
    @ClusteringColumn(2)
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(3)
    @Column(name = "local_id")
    private String localId = null;
    @ClusteringColumn(0)
    @Column(name = "nhs_number")
    private String nhsNumber = null;
    @Column(name = "person_id")
    private UUID personId = null;
    @ClusteringColumn(4)
    @Column(name = "timestamp")
    private Date timestamp = null;

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
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

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
