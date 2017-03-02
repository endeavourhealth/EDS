package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "transform", name = "enterprise_id_map")
public class EnterpriseIdMap {

    @PartitionKey
    @Column(name = "enterprise_table_name")
    private String enterpriseTableName = null;

    @ClusteringColumn(0)
    @Column(name = "resource_type")
    private String resourceType = null;

    @ClusteringColumn(1)
    @Column(name = "resource_id")
    private UUID resourceId = null;

    @Column(name = "enterprise_id")
    private Integer enterpriseId = null;

    public EnterpriseIdMap() {}

    public String getEnterpriseTableName() {
        return enterpriseTableName;
    }

    public void setEnterpriseTableName(String enterpriseTableName) {
        this.enterpriseTableName = enterpriseTableName;
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

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
