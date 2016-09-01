package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.util.Date;

public class Medication extends AbstractModel {

    private Long serviceProviderId;
    private Long patientId;
    private Date issueDate;
    private Long dmdCode;
    private String medicationTerm;
    private String bnfChapter;
    private Integer issueCount;
    private Double quantity;
    private String unit;
    private Double cost;
    private Long staffId;
    private Integer medicationIssueId;

    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("SK_PatientID", csvPrinter);
        printString("IssueDate", csvPrinter);
        printString("IssueTime", csvPrinter);
        printString("DMDCode", csvPrinter);
        printString("MedicationTerm", csvPrinter);
        printString("BNFChapter", csvPrinter);
        printString("IssueCount", csvPrinter);
        printString("Quantity", csvPrinter);
        printString("Unit", csvPrinter);
        printString("Cost", csvPrinter);
        printString("SK_StaffID", csvPrinter);
        printString("SK_MedicationIssueID", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printLong(serviceProviderId, csvPrinter);
        printLong(patientId, csvPrinter);
        printDate(issueDate, csvPrinter);
        printTime(issueDate, csvPrinter);
        printLong(dmdCode, csvPrinter);
        printString(medicationTerm, csvPrinter);
        printString(bnfChapter, csvPrinter);
        printInt(issueCount, csvPrinter);
        printDouble(quantity, csvPrinter);
        printString(unit, csvPrinter);
        printDouble(cost, csvPrinter);
        printLong(staffId, csvPrinter);
        printInt(medicationIssueId, csvPrinter);
    }

    @Override
    public Long getServiceProviderId() {
        return serviceProviderId;
    }

    @Override
    public void setServiceProviderId(Long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Long getDmdCode() {
        return dmdCode;
    }

    public void setDmdCode(Long dmdCode) {
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

    public Integer getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(Integer issueCount) {
        this.issueCount = issueCount;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Integer getMedicationIssueId() {
        return medicationIssueId;
    }

    public void setMedicationIssueId(Integer medicationIssueId) {
        this.medicationIssueId = medicationIssueId;
    }
}
