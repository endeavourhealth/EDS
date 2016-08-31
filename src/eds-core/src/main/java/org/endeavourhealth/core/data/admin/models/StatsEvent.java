package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "admin", name = "audit")
public class StatsEvent {
    @PartitionKey
    @Column(name = "id")
    private UUID id;

    @Column(name = "organisation")
    private String organisation;

    @Column(name = "observation")
    private String observation;

    @Column(name = "medication")
    private String medication;

    @Column(name = "condition")
    private String condition;

    public static StatsEvent factoryNow(UUID endUserUuid, UUID organisationUuid) {
        StatsEvent ret = new StatsEvent();
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

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}