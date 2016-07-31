package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Prescribing_IssueRecord extends AbstractCsvTransformer {

    public Prescribing_IssueRecord(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "IssueRecordGuid",
                "PatientGuid",
                "OrganisationGuid",
                "DrugRecordGuid",
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
                "CourseDurationInDays",
                "EstimatedNhsCost",
                "IsConfidential",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getIssueRecordGuid() {
        return super.getString("IssueRecordGuid");
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
    public Double getQuantity() {
        return super.getDouble("Quantity");
    }
    public Integer getCourseDurationInDays() {
        return super.getInt("CourseDurationInDays");
    }
    public Double getEstimatedNhsCost() {
        return super.getDouble("EstimatedNhsCost");
    }
    public String getProblemObservationGuid() {
        return super.getString("ProblemObservationGuid");
    }
    public String getDosage() {
        return super.getString("Dosage");
    }
    public String getQuantityUnit() {
        return super.getString("QuantityUnit");
    }
    public String getDrugRecordGuid() {
        return super.getString("DrugRecordGuid");
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
