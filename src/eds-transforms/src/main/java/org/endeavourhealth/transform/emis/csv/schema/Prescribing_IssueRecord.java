package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class Prescribing_IssueRecord extends AbstractCsvTransformer {
    public Prescribing_IssueRecord(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getIssueRecordGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getPatientGuid() {
        return super.getUniqueIdentifier(1);
    }
    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(2);
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
    public UUID getClinicianUserInRoleGuid() {
        return super.getUniqueIdentifier(7);
    }
    public UUID getEnteredByUserInRoleGuid() {
        return super.getUniqueIdentifier(8);
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
    public UUID getProblemObservationGuid() {
        return super.getUniqueIdentifier(13);
    }
    public String getDosage() {
        return super.getString(14);
    }
    public String getQuantityUnit() {
        return super.getString(15);
    }
    public UUID getDrugRecordGuid() {
        return super.getUniqueIdentifier(16);
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
