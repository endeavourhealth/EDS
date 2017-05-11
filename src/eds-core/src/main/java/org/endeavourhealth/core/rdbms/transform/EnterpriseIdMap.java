package org.endeavourhealth.core.rdbms.transform;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "enterprise_id_map", schema = "public")
public class EnterpriseIdMap implements Serializable {

    //private String enterpriseTableName = null;
    private String resourceId = null;
    private String resourceType = null;
    private Long enterpriseId = null;

    /*@Id
    @Column(name = "enterprise_table_name", nullable = false)
    public String getEnterpriseTableName() {
        return enterpriseTableName;
    }

    public void setEnterpriseTableName(String enterpriseTableName) {
        this.enterpriseTableName = enterpriseTableName;
    }*/

    @Id
    @Column(name = "resource_id", nullable = false)
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Id
    @Column(name = "resource_type", nullable = false)
    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Generated(GenerationTime.INSERT)
    @Column(name = "enterprise_id", insertable = false)
    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
