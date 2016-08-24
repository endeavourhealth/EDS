package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "ehr", name = "resource_types_used")
public class ResourceTypesUsed {

    @PartitionKey(0)
    @Column(name = "service_id")
    private UUID serviceId = null;

    @ClusteringColumn(0)
    @Column(name = "system_id")
    private UUID systemId = null;

    @ClusteringColumn(1)
    @Column(name = "resource_type")
    private String resourceType = null;


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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
