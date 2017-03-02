package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.endeavourhealth.core.data.ehr.HasResourceDataJson;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "resource_history")
public class ResourceHistory implements HasResourceDataJson {
    @PartitionKey(0)
    @Column(name = "resource_id")
    private UUID resourceId;

    @PartitionKey(1)
    @Column(name = "resource_type")
    private String resourceType;

    @ClusteringColumn
    private UUID version;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "system_id")
    private UUID systemId;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "schema_version")
    private String schemaVersion;

    @Column(name = "resource_data")
    private String resourceData;

    @Column(name = "resource_checksum")
    private long resourceChecksum;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

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

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
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

    public long getResourceChecksum() {
        return resourceChecksum;
    }

    public void setResourceChecksum(long resourceChecksum) {
        this.resourceChecksum = resourceChecksum;
    }
}
