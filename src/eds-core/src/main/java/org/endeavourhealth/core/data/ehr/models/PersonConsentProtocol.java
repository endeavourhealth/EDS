package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "person_consent_organisation")
public class PersonConsentProtocol {

    @PartitionKey
    @Column(name = "person_id")
    private UUID personId = null;
    @ClusteringColumn(0)
    @Column(name = "protocol_id")
    private UUID protocolId = null;
    @ClusteringColumn(1)
    @Column(name = "timestamp")
    private Date timestamp = null;
    @Column(name = "consent_given")
    private Boolean consentGiven = null;

    public PersonConsentProtocol() {}

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public UUID getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(UUID protocolId) {
        this.protocolId = protocolId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }
}
