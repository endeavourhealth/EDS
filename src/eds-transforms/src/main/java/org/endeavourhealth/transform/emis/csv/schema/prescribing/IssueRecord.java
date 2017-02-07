package org.endeavourhealth.transform.emis.csv.schema.prescribing;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class IssueRecord extends AbstractCsvParser {

    public IssueRecord(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {

        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_0)) {
            return new String[]{
                    "IssueRecordGuid",
                    "PatientGuid",
                    "OrganisationGuid",
                    "DrugRecordGuid",
                    "EffectiveDate",
                    "EffectiveDatePrecision",
                    "EnteredDate",
                    //"EnteredTime", //not present in this earlier version
                    "ClinicanUserInRoleGuid", //mis-spelled column name
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
        } else if (version.equals(EmisCsvToFhirTransformer.VERSION_5_1)) {
            return new String[]{
                    "IssueRecordGuid",
                    "PatientGuid",
                    "OrganisationGuid",
                    "DrugRecordGuid",
                    "EffectiveDate",
                    "EffectiveDatePrecision",
                    "EnteredDate",
                    "EnteredTime",
                    "ClinicanUserInRoleGuid", //mis-spelled column name
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
        } else {
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
    public Date getEnteredDate() throws TransformException {
        return super.getDate("EnteredDate");
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

    /**
     * special function for handling mis-spelt column in EMIS test pack
     */
    public String getClinicanUserInRoleGuid() {
        return super.getString("ClinicanUserInRoleGuid");
    }
}
