package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "enterprise_organisation_id_map", schema = "public", catalog = "transform")
public class EnterpriseOrganisationIdMap implements Serializable {

    private String organisationOdsCode = null;
    private Integer enterpriseId = null;

    @Id
    @Column(name = "ods_code", nullable = false)
    public String getOrganisationOdsCode() {
        return organisationOdsCode;
    }

    public void setOrganisationOdsCode(String organisationOdsCode) {
        this.organisationOdsCode = organisationOdsCode;
    }

    @Column(name = "enterprise_id", nullable = false)
    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
