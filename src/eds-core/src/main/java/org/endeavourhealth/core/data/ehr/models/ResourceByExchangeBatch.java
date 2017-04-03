package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.endeavourhealth.core.data.ehr.HasResourceDataJson;

import java.util.UUID;

@Table(keyspace = "ehr", name = "resource_by_exchange_batch")
public class ResourceByExchangeBatch implements HasResourceDataJson {
    @PartitionKey
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "exchange_id")
    private UUID exchangeId;

    @ClusteringColumn(0)
    @Column(name = "resource_type")
    private String resourceType;

    @ClusteringColumn(1)
    @Column(name = "resource_id")
    private UUID resourceId;

    @ClusteringColumn(2)
    private UUID version;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "schema_version")
    private String schemaVersion;

    @Column(name = "resource_data")
    private String resourceData;

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public UUID getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(UUID exchangeId) {
        this.exchangeId = exchangeId;
    }

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
}
