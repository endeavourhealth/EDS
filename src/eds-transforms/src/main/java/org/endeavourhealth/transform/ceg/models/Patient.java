package org.endeavourhealth.transform.ceg.models;

import java.util.Date;

public class Patient {

    private long serviceProviderId;
    private long serviceProviderIdPseudo;
    private long patientId;
    private long patientIdPseudo;
    private Date dateOfBirth;
    private int yearOfDeath;
    private String gender;
    private String lowerSuperOutputArea;
    private Date dateRegistered;
    private Date dateRegisteredEnd;
    private int consentBitmask;

    public long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public long getServiceProviderIdPseudo() {
        return serviceProviderIdPseudo;
    }

    public void setServiceProviderIdPseudo(long serviceProviderIdPseudo) {
        this.serviceProviderIdPseudo = serviceProviderIdPseudo;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public long getPatientIdPseudo() {
        return patientIdPseudo;
    }

    public void setPatientIdPseudo(long patientIdPseudo) {
        this.patientIdPseudo = patientIdPseudo;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getYearOfDeath() {
        return yearOfDeath;
    }

    public void setYearOfDeath(int yearOfDeath) {
        this.yearOfDeath = yearOfDeath;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLowerSuperOutputArea() {
        return lowerSuperOutputArea;
    }

    public void setLowerSuperOutputArea(String lowerSuperOutputArea) {
        this.lowerSuperOutputArea = lowerSuperOutputArea;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public Date getDateRegisteredEnd() {
        return dateRegisteredEnd;
    }

    public void setDateRegisteredEnd(Date dateRegisteredEnd) {
        this.dateRegisteredEnd = dateRegisteredEnd;
    }

    public int getConsentBitmask() {
        return consentBitmask;
    }

    public void setConsentBitmask(int consentBitmask) {
        this.consentBitmask = consentBitmask;
    }
}
