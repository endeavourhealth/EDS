package org.endeavourhealth.core.data.ehr.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "ehr", name = "person_resource_metadata_by_date")
public class PersonResourceMetadata {
    @PartitionKey
    @Column(name = "person_id")
    private UUID personId;
    @ClusteringColumn(0)
    @Column(name = "resource_type")
    private String resourceType;
    @ClusteringColumn(1)
    @Column(name = "effective_date")
    private Date effectiveDate;
    @ClusteringColumn(2)
    @Column(name = "service_id")
    private UUID serviceId;
    @ClusteringColumn(3)
    @Column(name = "system_instance_id")
    private UUID systemInstanceId;
    @ClusteringColumn(4)
    @Column(name = "resource_id")
    private String resourceId;
    @Column(name = "resource_metadata")
    private String resourceMetadata;

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

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
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

    public String getResourceMetadata() {
        return resourceMetadata;
    }

    public void setResourceMetadata(String resourceMetadata) {
        this.resourceMetadata = resourceMetadata;
    }
}
