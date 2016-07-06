package org.endeavourhealth.patientui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonService {
    private String serviceId = null;
    private String serviceName = null;
    private String organisationId = null;
    private String organisationName = null;
    private String organisationNationalId = null;

    public JsonService() {}

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOrganisationNationalId() {
        return organisationNationalId;
    }

    public void setOrganisationNationalId(String organisationNationalId) {
        this.organisationNationalId = organisationNationalId;
    }
}
