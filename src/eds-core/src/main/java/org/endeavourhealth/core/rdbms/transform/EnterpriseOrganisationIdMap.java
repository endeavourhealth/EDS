package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "enterprise_organisation_id_map", schema = "public", catalog = "transform")
public class EnterpriseOrganisationIdMap implements Serializable {

    private String odsCode = null;
    private Long enterpriseId = null;

    @Id
    @Column(name = "ods_code", nullable = false)
    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }

    @Column(name = "enterprise_id", nullable = false)
    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
