package org.endeavourhealth.transform.enterprise;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;

import java.util.Map;
import java.util.UUID;

public class EnterpriseTransformParams {

    private final UUID protocolId;
    private final String enterpriseConfigName;
    private final OutputContainer data;
    private final Map<String, ResourceByExchangeBatch> allResources;

    private int batchSize;
    private Long enterpriseOrganisationId = null;
    private Long enterprisePatientId = null;
    private Long enterprisePersonId = null;


    public EnterpriseTransformParams(UUID protocolId, String enterpriseConfigName, OutputContainer data, Map<String, ResourceByExchangeBatch> allResources) {
        this.protocolId = protocolId;
        this.enterpriseConfigName = enterpriseConfigName;
        this.data = data;
        this.allResources = allResources;
    }

    public UUID getProtocolId() {
        return protocolId;
    }

    public String getEnterpriseConfigName() {
        return enterpriseConfigName;
    }

    public OutputContainer getData() {
        return data;
    }

    public Map<String, ResourceByExchangeBatch> getAllResources() {
        return allResources;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Long getEnterpriseOrganisationId() {
        return enterpriseOrganisationId;
    }

    public void setEnterpriseOrganisationId(Long enterpriseOrganisationId) {
        if (this.enterpriseOrganisationId != null) {
            throw new IllegalArgumentException("Cannot change the enterpriseOrganisationId once set");
        }
        this.enterpriseOrganisationId = enterpriseOrganisationId;
    }

    public Long getEnterprisePatientId() {
        return enterprisePatientId;
    }

    public void setEnterprisePatientId(Long enterprisePatientId) {
        if (this.enterprisePatientId != null) {
            throw new IllegalArgumentException("Cannot change the enterprisePatientId once set");
        }
        this.enterprisePatientId = enterprisePatientId;
    }

    public Long getEnterprisePersonId() {
        return enterprisePersonId;
    }

    public void setEnterprisePersonId(Long enterprisePersonId) {
        if (this.enterprisePersonId != null) {
            throw new IllegalArgumentException("Cannot change the enterprisePersonId once set");
        }
        this.enterprisePersonId = enterprisePersonId;
    }
}
