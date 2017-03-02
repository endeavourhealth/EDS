package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Table(keyspace = "admin", name = "service")
public class Service {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "local_identifier")
    private String localIdentifier;
    @Column(name = "name")
    private String name;
    @Column(name = "endpoints")
    private String endpoints;
    @Column(name = "organisations")
    private Map<UUID, String> organisations = new HashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<UUID, String> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Map<UUID, String> organisations) {
        if (organisations == null)
            this.organisations = new HashMap<>();
        else
            this.organisations = organisations;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public void setLocalIdentifier(String localIdentifier) {
        this.localIdentifier = localIdentifier;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }
}