package org.endeavourhealth.core.rdbms.transform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "enterprise_organisation_id_map", schema = "public")
public class EnterpriseOrganisationIdMap implements Serializable {

    private String serviceId = null;
    private String systemId = null;
    //private String enterpriseConfigName = null;
    //private String odsCode = null;
    private Long enterpriseId = null;

    @Id
    @Column(name = "service_id", nullable = false)
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    @Column(name = "system_id", nullable = false)
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /*@Id
    @Column(name = "enterprise_config_name", nullable = false)
    public String getEnterpriseConfigName() {
        return enterpriseConfigName;
    }

    public void setEnterpriseConfigName(String enterpriseConfigName) {
        this.enterpriseConfigName = enterpriseConfigName;
    }*/

    /*@Id
    @Column(name = "ods_code", nullable = false)
    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }*/

    @Column(name = "enterprise_id", nullable = false)
    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
