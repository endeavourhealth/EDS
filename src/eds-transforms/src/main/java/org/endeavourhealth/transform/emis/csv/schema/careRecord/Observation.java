package org.endeavourhealth.transform.emis.csv.schema.careRecord;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class Observation extends AbstractCsvParser {

    public Observation(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "ObservationGuid",
                "PatientGuid",
                "OrganisationGuid",
                "EffectiveDate",
                "EffectiveDatePrecision",
                "EnteredDate",
                "EnteredTime",
                "ClinicianUserInRoleGuid",
                "EnteredByUserInRoleGuid",
                "ParentObservationGuid",
                "CodeId",
                "ProblemGuid",
                "AssociatedText",
                "ConsultationGuid",
                "Value",
                "NumericUnit",
                "ObservationType",
                "NumericRangeLow",
                "NumericRangeHigh",
                "DocumentGuid",
                "Deleted",
                "IsConfidential",
                "ProcessingId"

        };
    }

    public String getObservationGuid() {
        return super.getString("ObservationGuid");
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
    public String getParentObservationGuid() {
        return super.getString("ParentObservationGuid");
    }
    public Long getCodeId() {
        return super.getLong("CodeId");
    }
    public String getProblemGuid() {
        return super.getString("ProblemGuid");
    }
    public String getAssociatedText() {
        return super.getString("AssociatedText");
    }
    public String getConsultationGuid() {
        return super.getString("ConsultationGuid");
    }
    public Double getValue() {
        return super.getDouble("Value");
    }
    public String getNumericUnit() {
        return super.getString("NumericUnit");
    }
    public String getObservationType() {
        return super.getString("ObservationType");
    }
    public Double getNumericRangeLow() {
        return super.getDouble("NumericRangeLow");
    }
    public Double getNumericRangeHigh() {
        return super.getDouble("NumericRangeHigh");
    }
    public String getDocumentGuid() {
        return super.getString("DocumentGuid");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public boolean getIsConfidential() {
        return super.getBoolean("IsConfidential");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
