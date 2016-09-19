package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDemographics {

    private UUID serviceId;
    private UUID systemId;
    private UUID patientId;
    private String nhsNumber;
    private String displayName;
    private String dateOfBirthString;
    private String genderString;

    public String getDisplayName() {
        return displayName;
    }

    public JsonDemographics setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDateOfBirthString() {
        return dateOfBirthString;
    }

    public JsonDemographics setDateOfBirthString(String dateOfBirthString) {
        this.dateOfBirthString = dateOfBirthString;
        return this;
    }

    public String getGenderString() {
        return genderString;
    }

    public JsonDemographics setGenderString(String genderString) {
        this.genderString = genderString;
        return this;
    }

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
