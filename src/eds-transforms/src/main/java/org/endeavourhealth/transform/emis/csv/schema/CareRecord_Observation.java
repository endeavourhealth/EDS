package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CareRecord_Observation extends AbstractCsvTransformer {

    public CareRecord_Observation(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public UUID getObservationGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getParentOvercastionGuid() {
        return super.getUniqueIdentifier(1);
    }
    public UUID getPatientGuid() {
        return super.getUniqueIdentifier(2);
    }
    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(3);
    }
    public Date getEffectiveDate() throws TransformException {
        return super.getDate(4);
    }
    public String getEffectiveDatePrecision() {
        return super.getString(5);
    }
    public Date getEnteredDateTime() throws TransformException {
        return super.getDateTime(6, 7);
    }
    public UUID getClinicianUserInRoleGuid() {
        return super.getUniqueIdentifier(8);
    }
    public UUID getEnteredByUserInRoleGuid() {
        return super.getUniqueIdentifier(9);
    }
    public Long getCodeId() {
        return super.getLong(10);
    }
    public UUID getProblemUGuid() {
        return super.getUniqueIdentifier(11);
    }
    public UUID getConsultationGuid() {
        return super.getUniqueIdentifier(12);
    }
    public Double getValue() {
        return super.getDouble(13);
    }
    public Double getNumericRangeLow() {
        return super.getDouble(14);
    }
    public Double getNumericRangeHigh() {
        return super.getDouble(15);
    }
    public String getNumericUnit() {
        return super.getString(16);
    }
    public String getObservationType() {
        return super.getString(17);
    }
    public String getAssociatedText() {
        return super.getString(18);
    }
    public boolean getDeleted() {
        return super.getBoolean(19);
    }
    public Integer getProcessingId() {
        return super.getInt(20);
    }
    public UUID getDocumentGuid() {
        return super.getUniqueIdentifier(21);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(22);
    }
}
