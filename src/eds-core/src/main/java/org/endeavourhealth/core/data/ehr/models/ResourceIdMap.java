package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "ehr", name = "resource_id_map")
public class ResourceIdMap {

    @PartitionKey
    @Column(name = "service_id")
    private UUID serviceId = null;
    @ClusteringColumn(0)
    @Column(name = "system_instance_id")
    private UUID systemInstanceId = null;
    @ClusteringColumn(1)
    @Column(name = "resource_type")
    private String resourceType = null;
    @ClusteringColumn(2)
    @Column(name = "source_id")
    private String sourceId = null;
    @Column(name = "eds_id")
    private UUID edsId = null;

    public ResourceIdMap() {}

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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public UUID getEdsId() {
        return edsId;
    }

    public void setEdsId(UUID edsId) {
        this.edsId = edsId;
    }
}
