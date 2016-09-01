package org.endeavourhealth.transform.ceg.models;

import java.util.Date;

public class PatientDemographics extends AbstractModel {

    private long serviceProviderId;
    private long patientId;
    private Date dateRegistered;
    private Date dateRegisteredEnd;
    private String patientStatus;
    private int patientStatusCode;
    private String gender;
    private String lsoaCode;
    private String ethnicityCode;
    private int yearOfDeath;
    private String usualGpName;

    public long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
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

    public int getPatientStatusCode() {
        return patientStatusCode;
    }

    public void setPatientStatusCode(int patientStatusCode) {
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

    public int getYearOfDeath() {
        return yearOfDeath;
    }

    public void setYearOfDeath(int yearOfDeath) {
        this.yearOfDeath = yearOfDeath;
    }

    public String getUsualGpName() {
        return usualGpName;
    }

    public void setUsualGpName(String usualGpName) {
        this.usualGpName = usualGpName;
    }
}
