package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Prescribing_IssueRecord extends AbstractCsvTransformer {
    public Prescribing_IssueRecord(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public String getIssueRecordGuid() {
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
    public Date getEffectiveDateTime() throws TransformException {
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
    public Double getQuantity() {
        return super.getDouble(10);
    }
    public Integer getCourseDurationInDays() {
        return super.getInt(11);
    }
    public Double getEstimatedNhsCost() {
        return super.getDouble(12);
    }
    public String getProblemObservationGuid() {
        return super.getString(13);
    }
    public String getDosage() {
        return super.getString(14);
    }
    public String getQuantityUnit() {
        return super.getString(15);
    }
    public String getDrugRecordGuid() {
        return super.getString(16);
    }
    public boolean getDeleted() {
        return super.getBoolean(17);
    }
    public Integer getProcessingId() {
        return super.getInt(18);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(19);
    }
}
