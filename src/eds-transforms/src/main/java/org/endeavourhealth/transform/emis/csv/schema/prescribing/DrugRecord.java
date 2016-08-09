package org.endeavourhealth.transform.emis.csv.schema.prescribing;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.util.Date;

public class DrugRecord extends AbstractCsvTransformer {
    public DrugRecord(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
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
                "IsConfidential",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getDrugRecordGuid() {
        return super.getString("DrugRecordGuid");
    }
    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public Date getEffectiveDate() throws TransformException {
        return super.getDate("EffectiveDate");
    }
    public String getEffectiveDatePrecision() {
        return super.getString("EffectiveDatePrecision");
    }
    public Date getEnteredDateTime() throws TransformException {
        return super.getDateTime("EnteredDate", "EnteredTime");
    }
    public String getClinicianUserInRoleGuid() {
        return super.getString("ClinicianUserInRoleGuid");
    }
    public String getEnteredByUserInRoleGuid() {
        return super.getString("EnteredByUserInRoleGuid");
    }
    public Long getCodeId() {
        return super.getLong("CodeId");
    }
    public String getDosage() {
        return super.getString("Dosage");
    }
    public Double getQuantity() {
        return super.getDouble("Quantity");
    }
    public String getQuantityUnit() {
        return super.getString("QuantityUnit");
    }
    public String getProblemObservationGuid() {
        return super.getString("ProblemObservationGuid");
    }
    public String getPrescriptionType() {
        return super.getString("PrescriptionType");
    }
    public boolean getIsActive() {
        return super.getBoolean("IsActive");
    }
    public Date getCancellationDate() throws TransformException {
        return super.getDate("CancellationDate");
    }
    public Integer getNumberOfIssues() {
        return super.getInt("NumberOfIssues");
    }
    public Integer getNumberOfIssuesAuthorised() {
        return super.getInt("NumberOfIssuesAuthorised");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
    public boolean getIsConfidential() {
        return super.getBoolean("IsConfidential");
    }
}
