package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import org.endeavourhealth.core.data.ehr.PersonSex;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "person_identifier")
public class PersonIdentifier {

    @ClusteringColumn(0)
    @Column(name = "organisation_id")
    private UUID organisationId = null;
    @ClusteringColumn(1)
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(2)
    @Column(name = "local_id")
    private String localId = null;
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
    @Column(name = "sex")
    private PersonSex sex = null;
    @Column(name = "person_id")
    private UUID personId = null;
    @ClusteringColumn(3)
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

    public PersonSex getSex() {
        return sex;
    }

    public void setSex(PersonSex sex) {
        this.sex = sex;
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
