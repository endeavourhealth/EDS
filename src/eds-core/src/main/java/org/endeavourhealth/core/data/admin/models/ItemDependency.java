package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "admin", name = "item_dependency")
public class ItemDependency {
    @PartitionKey
    @Column(name = "item_id")
    private UUID itemId;
    @ClusteringColumn(0)
    @Column(name = "audit_id")
    private UUID auditId;
    @ClusteringColumn(1)
    @Column(name = "dependent_item_id")
    private UUID dependentItemId;
    @Column(name = "dependency_type_id")
    private Integer dependencyTypeId;


    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }

    public UUID getDependentItemId() {
        return dependentItemId;
    }

    public void setDependentItemId(UUID dependentItemId) {
        this.dependentItemId = dependentItemId;
    }

    public Integer getDependencyTypeId() {
        return dependencyTypeId;
    }

    public void setDependencyTypeId(Integer dependencyTypeId) {
        this.dependencyTypeId = dependencyTypeId;
    }

}