package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;
import java.util.Date;

public class PatientDemographics extends AbstractModel {

    private BigInteger serviceProviderId;
    private BigInteger patientId;
    private Date dateRegistered;
    private Date dateRegisteredEnd;
    private String patientStatus;
    private String patientStatusCode;
    private String gender;
    private String lsoaCode;
    private String ethnicityCode;
    private Integer yearOfDeath;
    private String usualGpName;


    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("SK_PatientID", csvPrinter);
        printString("DateRegistered", csvPrinter);
        printString("DateRegisteredEnd", csvPrinter);
        printString("PatientStatus", csvPrinter);
        printString("PatientStatusCode", csvPrinter);
        printString("Gender", csvPrinter);
        printString("LSOACode", csvPrinter);
        printString("EthnicityCode", csvPrinter);
        printString("YearOfDeath", csvPrinter);
        printString("UsualGPName", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printBigInt(serviceProviderId, csvPrinter);
        printBigInt(patientId, csvPrinter);
        printDate(dateRegistered, csvPrinter);
        printDate(dateRegisteredEnd, csvPrinter);
        printString(patientStatus, csvPrinter);
        printString(patientStatusCode, csvPrinter);
        printString(gender, csvPrinter);
        printString(lsoaCode, csvPrinter);
        printString(ethnicityCode, csvPrinter);
        printInt(yearOfDeath, csvPrinter);
        printString(usualGpName, csvPrinter);
    }

    @Override
    public BigInteger getServiceProviderId() {
        return serviceProviderId;
    }

    @Override
    public void setServiceProviderId(BigInteger serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public BigInteger getPatientId() {
        return patientId;
    }

    public void setPatientId(BigInteger patientId) {
        this.patientId = patientId;
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

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public String getPatientStatusCode() {
        return patientStatusCode;
    }

    public void setPatientStatusCode(String patientStatusCode) {
        this.patientStatusCode = patientStatusCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLsoaCode() {
        return lsoaCode;
    }

    public void setLsoaCode(String lsoaCode) {
        this.lsoaCode = lsoaCode;
    }

    public String getEthnicityCode() {
        return ethnicityCode;
    }

    public void setEthnicityCode(String ethnicityCode) {
        this.ethnicityCode = ethnicityCode;
    }

    public Integer getYearOfDeath() {
        return yearOfDeath;
    }

    public void setYearOfDeath(Integer yearOfDeath) {
        this.yearOfDeath = yearOfDeath;
    }

    public String getUsualGpName() {
        return usualGpName;
    }

    public void setUsualGpName(String usualGpName) {
        this.usualGpName = usualGpName;
    }
}
