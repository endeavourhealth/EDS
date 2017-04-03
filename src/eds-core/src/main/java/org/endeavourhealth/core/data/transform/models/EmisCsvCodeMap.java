package org.endeavourhealth.core.data.transform.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "transform", name = "emis_csv_code_map")
public class EmisCsvCodeMap {

    @PartitionKey(0)
    @Column(name = "data_sharing_agreement_guid")
    private String dataSharingAgreementGuid = null;
    @ClusteringColumn(0)
    @Column(name = "medication")
    private boolean medication = false;
    @ClusteringColumn(1)
    @Column(name = "code_id")
    private Long codeId = null;
    @ClusteringColumn(2)
    @Column(name = "time_uuid")
    private UUID timeUuid = null;
    @Column(name = "code_type")
    private String codeType = null;
    @Column(name = "codeable_concept")
    private String codeableConcept = null;
    @Column(name = "read_term")
    private String readTerm = null;
    @Column(name = "read_code")
    private String readCode = null;
    @Column(name = "snomed_concept_id")
    private Long snomedConceptId = null;
    @Column(name = "snomed_description_id")
    private Long snomedDescriptionId = null;
    @Column(name = "snomed_term")
    private String snomedTerm = null;
    @Column(name = "national_code")
    private String nationalCode = null;
    @Column(name = "national_code_category")
    private String nationalCodeCategory = null;
    @Column(name = "national_code_description")
    private String nationalCodeDescription = null;
    @Column(name = "parent_code_id")
    private Long parentCodeId = null;

    public EmisCsvCodeMap() {}

    public String getDataSharingAgreementGuid() {
        return dataSharingAgreementGuid;
    }

    public void setDataSharingAgreementGuid(String dataSharingAgreementGuid) {
        this.dataSharingAgreementGuid = dataSharingAgreementGuid;
    }

    public boolean isMedication() {
        return medication;
    }

    public void setMedication(boolean medication) {
        this.medication = medication;
    }

    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public UUID getTimeUuid() {
        return timeUuid;
    }

    public void setTimeUuid(UUID timeUuid) {
        this.timeUuid = timeUuid;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCodeableConcept() {
        return codeableConcept;
    }

    public void setCodeableConcept(String codeableConcept) {
        this.codeableConcept = codeableConcept;
    }

    public String getReadTerm() {
        return readTerm;
    }

    public void setReadTerm(String readTerm) {
        this.readTerm = readTerm;
    }

    public String getReadCode() {
        return readCode;
    }

    public void setReadCode(String readCode) {
        this.readCode = readCode;
    }

    public Long getSnomedConceptId() {
        return snomedConceptId;
    }

    public void setSnomedConceptId(Long snomedConceptId) {
        this.snomedConceptId = snomedConceptId;
    }

    public Long getSnomedDescriptionId() {
        return snomedDescriptionId;
    }

    public void setSnomedDescriptionId(Long snomedDescriptionId) {
        this.snomedDescriptionId = snomedDescriptionId;
    }

    public String getSnomedTerm() {
        return snomedTerm;
    }

    public void setSnomedTerm(String snomedTerm) {
        this.snomedTerm = snomedTerm;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getNationalCodeCategory() {
        return nationalCodeCategory;
    }

    public void setNationalCodeCategory(String nationalCodeCategory) {
        this.nationalCodeCategory = nationalCodeCategory;
    }

    public String getNationalCodeDescription() {
        return nationalCodeDescription;
    }

    public void setNationalCodeDescription(String nationalCodeDescription) {
        this.nationalCodeDescription = nationalCodeDescription;
    }

    public Long getParentCodeId() {
        return parentCodeId;
    }

    public void setParentCodeId(Long parentCodeId) {
        this.parentCodeId = parentCodeId;
    }
}
