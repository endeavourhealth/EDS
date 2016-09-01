package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.util.Date;

public class Patient extends AbstractModel {

    private Long serviceProviderId;
    private Long serviceProviderIdPseudo;
    private Long patientId;
    private Long patientIdPseudo;
    private Date dateOfBirth;
    private Integer yearOfDeath;
    private String gender;
    private String lowerSuperOutputArea;
    private Date dateRegistered;
    private Date dateRegisteredEnd;
    private Integer consentBitmask;

    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("SK_ServiceProviderID_Pseudo", csvPrinter);
        printString("SK_PatientID", csvPrinter);
        printString("SK_PatientID_Pseudo", csvPrinter);
        printString("DateOfBirth", csvPrinter);
        printString("YearOfDeath", csvPrinter);
        printString("Gender", csvPrinter);
        printString("LowerSuperOutputArea", csvPrinter);
        printString("DateRegistered", csvPrinter);
        printString("DateRegisteredEnd", csvPrinter);
        printString("ConsentBitmask", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printLong(serviceProviderId, csvPrinter);
        printLong(serviceProviderIdPseudo, csvPrinter);
        printLong(patientId, csvPrinter);
        printLong(patientIdPseudo, csvPrinter);
        printDate(dateOfBirth, csvPrinter);
        printInt(yearOfDeath, csvPrinter);
        printString(gender, csvPrinter);
        printString(lowerSuperOutputArea, csvPrinter);
        printDate(dateRegistered, csvPrinter);
        printDate(dateRegisteredEnd, csvPrinter);
        printInt(consentBitmask, csvPrinter);
    }

    @Override
    public Long getServiceProviderId() {
        return serviceProviderId;
    }

    @Override
    public void setServiceProviderId(Long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public Long getServiceProviderIdPseudo() {
        return serviceProviderIdPseudo;
    }

    public void setServiceProviderIdPseudo(Long serviceProviderIdPseudo) {
        this.serviceProviderIdPseudo = serviceProviderIdPseudo;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPatientIdPseudo() {
        return patientIdPseudo;
    }

    public void setPatientIdPseudo(Long patientIdPseudo) {
        this.patientIdPseudo = patientIdPseudo;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getYearOfDeath() {
        return yearOfDeath;
    }

    public void setYearOfDeath(Integer yearOfDeath) {
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

    public Integer getConsentBitmask() {
        return consentBitmask;
    }

    public void setConsentBitmask(Integer consentBitmask) {
        this.consentBitmask = consentBitmask;
    }
}
