package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "person_resource_event_store")
public class PersonResourceEventStore {
    @PartitionKey
    @Column(name = "person_id")
    private UUID personId;
    @ClusteringColumn(0)
    @Column(name = "resource_type")
    private String resourceType;
    @ClusteringColumn(1)
    @Column(name = "service_id")
    private UUID serviceId;
    @ClusteringColumn(2)
    @Column(name = "system_instance_id")
    private UUID systemInstanceId;
    @ClusteringColumn(3)
    @Column(name = "resource_id")
    private String resourceId;
    @ClusteringColumn(4)
    private String version;
    private Date created;
    private EventStoreMode mode;
    @Column(name = "resource_metadata")
    private String resourceMetadata;
    @Column(name = "schema_version")
    private String schemaVersion;
    @Column(name = "resource_data")
    private String resourceData;

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getSystemInstanceId() {
        return systemInstanceId;
    }

    public void setSystemInstanceId(UUID systemInstanceId) {
        this.systemInstanceId = systemInstanceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public EventStoreMode getMode() {
        return mode;
    }

    public void setMode(EventStoreMode mode) {
        this.mode = mode;
    }

    public String getResourceMetadata() {
        return resourceMetadata;
    }

    public void setResourceMetadata(String resourceMetadata) {
        this.resourceMetadata = resourceMetadata;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getResourceData() {
        return resourceData;
    }

    public void setResourceData(String resourceData) {
        this.resourceData = resourceData;
    }
}

