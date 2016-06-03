package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "admin", name = "organisation_end_user_link")
public class OrganisationEndUserLink {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "organisation_id")
    private UUID organisationId;
    @Column(name = "end_user_id")
    private UUID endUserId;
    @Column(name = "is_admin")
    private Boolean isAdmin;
    @Column(name = "dt_expired")
    private Date dtExpired;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public UUID getEndUserId() {
        return endUserId;
    }

    public void setEndUserId(UUID endUserId) {
        this.endUserId = endUserId;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Date getDtExpired() {
        return dtExpired;
    }

    public void setDtExpired(Date dtExpired) {
        this.dtExpired = dtExpired;
    }



}