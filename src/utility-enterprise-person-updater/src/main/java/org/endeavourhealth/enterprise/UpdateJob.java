package org.endeavourhealth.enterprise;

public class UpdateJob {
    private Long enterprisePatientId = null;
    private Long oldEnterprisePersonId = null;
    private Long newEnterprisePersonId = null;

    public UpdateJob(Long enterprisePatientId, Long oldEnterprisePersonId, Long newEnterprisePersonId) {
        this.enterprisePatientId = enterprisePatientId;
        this.oldEnterprisePersonId = oldEnterprisePersonId;
        this.newEnterprisePersonId = newEnterprisePersonId;

    }

    public Long getEnterprisePatientId() {
        return enterprisePatientId;
    }

    public Long getOldEnterprisePersonId() {
        return oldEnterprisePersonId;
    }

    public Long getNewEnterprisePersonId() {
        return newEnterprisePersonId;
    }
}
