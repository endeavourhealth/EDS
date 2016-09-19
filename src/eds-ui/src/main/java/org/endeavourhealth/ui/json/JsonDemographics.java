package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDemographics {

    private UUID serviceId;
    private UUID systemId;
    private UUID patientId;
    private String nhsNumber;

    public UUID getServiceId() {
        return serviceId;
    }

    public JsonDemographics setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public JsonDemographics setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public JsonDemographics setPatientId(UUID patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public JsonDemographics setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
        return this;
    }
}
