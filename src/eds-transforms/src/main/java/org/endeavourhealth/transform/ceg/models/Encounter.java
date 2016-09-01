package org.endeavourhealth.transform.ceg.models;

import java.util.Date;

public class Encounter extends AbstractModel {

    private long serviceProviderId;
    private long patientId;
    private Date eventDate;
    private Date eventTime;
    private String nativeClinicalCode;
    private double value;
    private String units;
    private int ageAtEvent;
    private boolean isDiaryEvent;
    private boolean isReferralEvent;
    private long staffId;
    private String consultationType;
    private int consultationDuration;
    private int problemId;
    private long snomedConceptCode;

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

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getNativeClinicalCode() {
        return nativeClinicalCode;
    }

    public void setNativeClinicalCode(String nativeClinicalCode) {
        this.nativeClinicalCode = nativeClinicalCode;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int getAgeAtEvent() {
        return ageAtEvent;
    }

    public void setAgeAtEvent(int ageAtEvent) {
        this.ageAtEvent = ageAtEvent;
    }

    public boolean isDiaryEvent() {
        return isDiaryEvent;
    }

    public void setDiaryEvent(boolean diaryEvent) {
        isDiaryEvent = diaryEvent;
    }

    public boolean isReferralEvent() {
        return isReferralEvent;
    }

    public void setReferralEvent(boolean referralEvent) {
        isReferralEvent = referralEvent;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public int getConsultationDuration() {
        return consultationDuration;
    }

    public void setConsultationDuration(int consultationDuration) {
        this.consultationDuration = consultationDuration;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public long getSnomedConceptCode() {
        return snomedConceptCode;
    }

    public void setSnomedConceptCode(long snomedConceptCode) {
        this.snomedConceptCode = snomedConceptCode;
    }
}
