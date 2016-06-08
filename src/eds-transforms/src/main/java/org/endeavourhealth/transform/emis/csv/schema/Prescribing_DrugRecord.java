package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Prescribing_DrugRecord extends AbstractCsvTransformer {
    public Prescribing_DrugRecord(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "DrugRecordGuid",
                "PatientGuid",
                "OrganisationGuid",
                "EffectiveDate",
                "EffectiveDatePrecision",
                "EnteredDate",
                "EnteredTime",
                "ClinicianUserInRoleGuid",
                "EnteredByUserInRoleGuid",
                "CodeId",
                "Dosage",
                "Quantity",
                "QuantityUnit",
                "ProblemObservationGuid",
                "PrescriptionType",
                "IsActive",
                "CancellationDate",
                "NumberOfIssues",
                "NumberOfIssuesAuthorised",
                "Deleted",
                "ProcessingId",
                "IsConfidential"
        };
    }

    public String getDrugRecordGuid() {
        return super.getString(0);
    }
    public String getPatientGuid() {
        return super.getString(1);
    }
    public String getOrganisationGuid() {
        return super.getString(2);
    }
    public Date getEffectiveDate() throws TransformException {
        return super.getDate(3);
    }
    public String getEffectiveDatePrecision() {
        return super.getString(4);
    }
    public Date getEnteredDateTime() throws TransformException {
        return super.getDateTime(5, 6);
    }
    public String getClinicianUserInRoleGuid() {
        return super.getString(7);
    }
    public String getEnteredByUserInRoleGuid() {
        return super.getString(8);
    }
    public Long getCodeId() {
        return super.getLong(9);
    }
    public String getDosage() {
        return super.getString(10);
    }
    public Double getQuantity() {
        return super.getDouble(11);
    }
    public String getQuantityUnit() {
        return super.getString(12);
    }
    public String getProblemObservationGuid() {
        return super.getString(13);
    }
    public String getPrescriptionType() {
        return super.getString(14);
    }
    public boolean getIsActive() {
        return super.getBoolean(15);
    }
    public Date getCancellationDate() throws TransformException {
        return super.getDate(16);
    }
    public Integer getNumberOfIssues() {
        return super.getInt(17);
    }
    public Integer getNumberOfIssuesAuthorised() {
        return super.getInt(18);
    }
    public boolean getDeleted() {
        return super.getBoolean(19);
    }
    public Integer getProcessingId() {
        return super.getInt(20);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(21);
    }
}
