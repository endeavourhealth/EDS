package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Table(keyspace = "admin", name = "audit")
public class Audit {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "end_user_id")
    private UUID endUserId;
    @Column(name = "time_stamp")
    private Date timeStamp;
    @Column(name = "audit_version")
    private Integer auditVersion;
    @Column(name = "organisation_id")
    private UUID organisationId;

    public static Audit factoryNow(UUID endUserUuid, UUID organisationUuid) {
        Audit ret = new Audit();
        ret.setId(UUID.randomUUID()); //always explicitly set a new UUID as we'll always want to use it
        ret.setEndUserId(endUserUuid);
        ret.setTimeStamp(Date.from(Instant.now()));
        ret.setOrganisationId(organisationUuid);
        return ret;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEndUserId() {
        return endUserId;
    }

    public void setEndUserId(UUID endUserId) {
        this.endUserId = endUserId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getAuditVersion() {
        return auditVersion;
    }

    public void setAuditVersion(Integer auditVersion) {
        this.auditVersion = auditVersion;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

}