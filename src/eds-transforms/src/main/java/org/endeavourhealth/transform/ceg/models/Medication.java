package org.endeavourhealth.transform.ceg.models;

import java.util.Date;

public class Medication extends AbstractModel {

    private long serviceProviderId;
    private long patientId;
    private Date issueDate;
    private Date issueTime;
    private long dmdCode;
    private String medicationTerm;
    private String bnfChapter;
    private int issueCount;
    private double quantity;
    private String unit;
    private double cost;
    private long staffId;
    private int medicationIssueId;

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

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public long getDmdCode() {
        return dmdCode;
    }

    public void setDmdCode(long dmdCode) {
        this.dmdCode = dmdCode;
    }

    public String getMedicationTerm() {
        return medicationTerm;
    }

    public void setMedicationTerm(String medicationTerm) {
        this.medicationTerm = medicationTerm;
    }

    public String getBnfChapter() {
        return bnfChapter;
    }

    public void setBnfChapter(String bnfChapter) {
        this.bnfChapter = bnfChapter;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(int issueCount) {
        this.issueCount = issueCount;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public int getMedicationIssueId() {
        return medicationIssueId;
    }

    public void setMedicationIssueId(int medicationIssueId) {
        this.medicationIssueId = medicationIssueId;
    }
}
