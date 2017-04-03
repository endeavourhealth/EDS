package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Date;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonDataSharingSummary {
    private String uuid = null;
    private String name = null;
    private String description = null;
    private String purpose = null;
    private Short natureOfInformationId = null;
    private String schedule2Condition = null;
    private String benefitToSharing = null;
    private String overviewOfDataItems = null;
    private Short formatTypeId = null;
    private Short dataSubjectTypeId = null;
    private String natureOfPersonsAccessingData = null;
    private Short reviewCycleId = null;
    private Date reviewDate = null;
    private Date startDate = null;
    private String evidenceOfAgreement = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Short getNatureOfInformationId() {
        return natureOfInformationId;
    }

    public void setNatureOfInformationId(Short natureOfInformationId) {
        this.natureOfInformationId = natureOfInformationId;
    }

    public String getSchedule2Condition() {
        return schedule2Condition;
    }

    public void setSchedule2Condition(String schedule2Condition) {
        this.schedule2Condition = schedule2Condition;
    }

    public String getBenefitToSharing() {
        return benefitToSharing;
    }

    public void setBenefitToSharing(String benefitToSharing) {
        this.benefitToSharing = benefitToSharing;
    }

    public String getOverviewOfDataItems() {
        return overviewOfDataItems;
    }

    public void setOverviewOfDataItems(String overviewOfDataItems) {
        this.overviewOfDataItems = overviewOfDataItems;
    }

    public Short getFormatTypeId() {
        return formatTypeId;
    }

    public void setFormatTypeId(Short formatTypeId) {
        this.formatTypeId = formatTypeId;
    }

    public Short getDataSubjectTypeId() {
        return dataSubjectTypeId;
    }

    public void setDataSubjectTypeId(Short dataSubjectTypeId) {
        this.dataSubjectTypeId = dataSubjectTypeId;
    }

    public String getNatureOfPersonsAccessingData() {
        return natureOfPersonsAccessingData;
    }

    public void setNatureOfPersonsAccessingData(String natureOfPersonsAccessingData) {
        this.natureOfPersonsAccessingData = natureOfPersonsAccessingData;
    }

    public Short getReviewCycleId() {
        return reviewCycleId;
    }

    public void setReviewCycleId(Short reviewCycleId) {
        this.reviewCycleId = reviewCycleId;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getEvidenceOfAgreement() {
        return evidenceOfAgreement;
    }

    public void setEvidenceOfAgreement(String evidenceOfAgreement) {
        this.evidenceOfAgreement = evidenceOfAgreement;
    }
}
