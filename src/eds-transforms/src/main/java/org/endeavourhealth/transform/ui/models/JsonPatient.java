package org.endeavourhealth.transform.ui.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonPatient {

    private UUID serviceId;
    private UUID systemId;
    private UUID patientId;
    private String nhsNumber;
    private String nhsNumberFormatted;
    private String title;
    private String forename;
    private String surname;
    private String displayName;
    private String dateOfBirthFormatted;
    private String genderFormatted;
    private String singleLineAddress;

    public String getDisplayName() {
        return displayName;
    }

    public JsonPatient setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDateOfBirthFormatted() {
        return dateOfBirthFormatted;
    }

    public JsonPatient setDateOfBirthFormatted(String dateOfBirthFormatted) {
        this.dateOfBirthFormatted = dateOfBirthFormatted;
        return this;
    }

    public String getGenderFormatted() {
        return genderFormatted;
    }

    public JsonPatient setGenderFormatted(String genderFormatted) {
        this.genderFormatted = genderFormatted;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public JsonPatient setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public JsonPatient setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public JsonPatient setPatientId(UUID patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public JsonPatient setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
        return this;
    }

    public String getNhsNumberFormatted() {
        return nhsNumberFormatted;
    }

    public JsonPatient setNhsNumberFormatted(String nhsNumberFormatted) {
        this.nhsNumberFormatted = nhsNumberFormatted;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public JsonPatient setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getForename() {
        return forename;
    }

    public JsonPatient setForename(String forename) {
        this.forename = forename;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public JsonPatient setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getSingleLineAddress() {
        return singleLineAddress;
    }

    public JsonPatient setSingleLineAddress(String singleLineAddress) {
        this.singleLineAddress = singleLineAddress;
        return this;
    }
}
