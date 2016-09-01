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
    private Integer patientStatusCode;
    private String gender;
    private String lsoaCode;
    private String ethnicityCode;
    private Integer yearOfDeath;
    private String usualGpName;


    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
        printString("", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {

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

    public Integer getPatientStatusCode() {
        return patientStatusCode;
    }

    public void setPatientStatusCode(Integer patientStatusCode) {
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
