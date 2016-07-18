package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CareRecord_Observation extends AbstractCsvTransformer {

    public CareRecord_Observation(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
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
    public String getParentObservationGuid() {
        return super.getString(9);
    }
    public Long getCodeId() {
        return super.getLong(10);
    }
    public String getProblemUGuid() {
        return super.getString(11);
    }
    public String getAssociatedText() {
        return super.getString(12);
    }
    public String getConsultationGuid() {
        return super.getString(13);
    }
    public Double getValue() {
        return super.getDouble(14);
    }
    public String getNumericUnit() {
        return super.getString(15);
    }
    public String getObservationType() {
        return super.getString(16);
    }
    public Double getNumericRangeLow() {
        return super.getDouble(17);
    }
    public Double getNumericRangeHigh() {
        return super.getDouble(18);
    }
    public String getDocumentGuid() {
        return super.getString(19);
    }
    public boolean getDeleted() {
        return super.getBoolean(20);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(21);
    }
    public Integer getProcessingId() {
        return super.getInt(22);
    }
}
