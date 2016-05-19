package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class PrescribingDrugRecord extends AbstractCsvTransformer {
    public PrescribingDrugRecord(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getDrugRecordGuid() {
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
    public Date getEnteredDateTime() throws TransformException {
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
    public String getDosage() {
        return super.getString(10);
    }
    public Double getQuantity() {
        return super.getDouble(11);
    }
    public String getQuantityUnit() {
        return super.getString(12);
    }
    public UUID getProblemObservationGuid() {
        return super.getUniqueIdentifier(13);
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
