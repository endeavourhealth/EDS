package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "admin", name = "audit")
public class StatsPatient {
    @PartitionKey
    @Column(name = "id")
    private UUID id;

    @Column(name = "organisation")
    private String organisation;

    @Column(name = "regular")
    private String regular;

    @Column(name = "left")
    private String left;

    @Column(name = "dead")
    private String dead;

    public static StatsPatient factoryNow(UUID endUserUuid, UUID organisationUuid) {
        StatsPatient ret = new StatsPatient();
        ret.setId(UUID.randomUUID()); //always explicitly set a new UUID as we'll always want to use it
        return ret;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getRegular() {
        return regular;
    }

    public void setRegular(String regular) {
        this.regular = regular;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getDead() {
        return dead;
    }

    public void setDead(String dead) {
        this.dead = dead;
    }

}