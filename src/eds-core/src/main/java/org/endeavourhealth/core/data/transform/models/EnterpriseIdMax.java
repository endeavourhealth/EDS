package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "transform", name = "enterprise_id_max")
public class EnterpriseIdMax {

    @PartitionKey
    @Column(name = "enterprise_table_name")
    private String enterpriseTableName = null;

    @Column(name = "max_enterprise_id")
    private Integer maxEnterpriseId = null;

    public EnterpriseIdMax() {}

    public String getEnterpriseTableName() {
        return enterpriseTableName;
    }

    public void setEnterpriseTableName(String enterpriseTableName) {
        this.enterpriseTableName = enterpriseTableName;
    }

    public Integer getMaxEnterpriseId() {
        return maxEnterpriseId;
    }

    public void setMaxEnterpriseId(Integer maxEnterpriseId) {
        this.maxEnterpriseId = maxEnterpriseId;
    }
}
