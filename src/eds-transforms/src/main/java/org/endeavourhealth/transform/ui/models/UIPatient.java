package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIPatient {

    private UUID serviceId;
    private UUID systemId;
    private UUID patientId;
    private String nhsNumber;
    private String nhsNumberFormatted;
    private String displayName;
    private String dateOfBirthFormatted;
    private String genderFormatted;
    private String singleLineAddress;

    public String getDisplayName() {
        return displayName;
    }

    public UIPatient setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDateOfBirthFormatted() {
        return dateOfBirthFormatted;
    }

    public UIPatient setDateOfBirthFormatted(String dateOfBirthFormatted) {
        this.dateOfBirthFormatted = dateOfBirthFormatted;
        return this;
    }

    public String getGenderFormatted() {
        return genderFormatted;
    }

    public UIPatient setGenderFormatted(String genderFormatted) {
        this.genderFormatted = genderFormatted;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UIPatient setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public UIPatient setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UIPatient setPatientId(UUID patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public UIPatient setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
        return this;
    }

    public String getNhsNumberFormatted() {
        return nhsNumberFormatted;
    }

    public UIPatient setNhsNumberFormatted(String nhsNumberFormatted) {
        this.nhsNumberFormatted = nhsNumberFormatted;
        return this;
    }

    public String getSingleLineAddress() {
        return singleLineAddress;
    }

    public UIPatient setSingleLineAddress(String singleLineAddress) {
        this.singleLineAddress = singleLineAddress;
        return this;
    }
}
