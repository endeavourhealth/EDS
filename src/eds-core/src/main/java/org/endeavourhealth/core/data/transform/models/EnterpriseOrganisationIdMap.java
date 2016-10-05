package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "transform", name = "enterprise_organisation_id_map")
public class EnterpriseOrganisationIdMap {

    @PartitionKey
    @Column(name = "organisation_ods_code")
    private String organisationOdsCode = null;

    @Column(name = "enterprise_id")
    private Integer enterpriseId = null;

    public EnterpriseOrganisationIdMap() {}

    public String getOrganisationOdsCode() {
        return organisationOdsCode;
    }

    public void setOrganisationOdsCode(String organisationOdsCode) {
        this.organisationOdsCode = organisationOdsCode;
    }

    public Integer getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Integer enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
