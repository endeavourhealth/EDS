package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;
import java.util.Date;

public class Encounter extends AbstractModel {

    private BigInteger serviceProviderId;
    private BigInteger patientId;
    private Date eventDate;
    private String nativeClinicalCode;
    private Double value;
    private String units;
    private Integer ageAtEvent;
    private Boolean isDiaryEvent;
    private Boolean isReferralEvent;
    private BigInteger staffId;
    private String consultationType;
    private Integer consultationDuration;
    private Integer problemId;
    private Long snomedConceptCode;


    @Override
    public void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception {
        printString("SK_ServiceProviderID", csvPrinter);
        printString("SK_PatientID", csvPrinter);
        printString("EventDate", csvPrinter);
        printString("EventTime", csvPrinter);
        printString("NativeClinicalCode", csvPrinter);
        printString("Value", csvPrinter);
        printString("Units", csvPrinter);
        printString("AgeAtEvent", csvPrinter);
        printString("IsDiaryEvent", csvPrinter);
        printString("IsReferralEvent", csvPrinter);
        printString("SK_StaffID", csvPrinter);
        printString("ConsultationType", csvPrinter);
        printString("ConsultationDuration", csvPrinter);
        printString("SK_ProblemID", csvPrinter);
        printString("SnomedConceptCode", csvPrinter);
    }

    @Override
    public void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception {
        printBigInt(serviceProviderId, csvPrinter);
        printBigInt(patientId, csvPrinter);
        printDate(eventDate, csvPrinter);
        printTime(eventDate, csvPrinter);
        printString(nativeClinicalCode, csvPrinter);
        printDouble(value, csvPrinter);
        printString(units, csvPrinter);
        printInt(ageAtEvent, csvPrinter);
        printBoolean(isDiaryEvent, csvPrinter);
        printBoolean(isReferralEvent, csvPrinter);
        printBigInt(staffId, csvPrinter);
        printString(consultationType, csvPrinter);
        printInt(consultationDuration, csvPrinter);
        printInt(problemId, csvPrinter);
        printLong(snomedConceptCode, csvPrinter);
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

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getNativeClinicalCode() {
        return nativeClinicalCode;
    }

    public void setNativeClinicalCode(String nativeClinicalCode) {
        this.nativeClinicalCode = nativeClinicalCode;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Integer getAgeAtEvent() {
        return ageAtEvent;
    }

    public void setAgeAtEvent(Integer ageAtEvent) {
        this.ageAtEvent = ageAtEvent;
    }

    public Boolean getDiaryEvent() {
        return isDiaryEvent;
    }

    public void setDiaryEvent(Boolean diaryEvent) {
        isDiaryEvent = diaryEvent;
    }

    public Boolean getReferralEvent() {
        return isReferralEvent;
    }

    public void setReferralEvent(Boolean referralEvent) {
        isReferralEvent = referralEvent;
    }

    public BigInteger getStaffId() {
        return staffId;
    }

    public void setStaffId(BigInteger staffId) {
        this.staffId = staffId;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public Integer getConsultationDuration() {
        return consultationDuration;
    }

    public void setConsultationDuration(Integer consultationDuration) {
        this.consultationDuration = consultationDuration;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public Long getSnomedConceptCode() {
        return snomedConceptCode;
    }

    public void setSnomedConceptCode(Long snomedConceptCode) {
        this.snomedConceptCode = snomedConceptCode;
    }
}
