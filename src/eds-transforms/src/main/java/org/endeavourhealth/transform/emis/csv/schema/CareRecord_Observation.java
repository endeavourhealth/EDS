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

    public String getObservationGuid() {
        return super.getString(0);
    }
    public String getParentOvercastionGuid() {
        return super.getString(1);
    }
    public String getPatientGuid() {
        return super.getString(2);
    }
    public String getOrganisationGuid() {
        return super.getString(3);
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
    public String getClinicianUserInRoleGuid() {
        return super.getString(8);
    }
    public String getEnteredByUserInRoleGuid() {
        return super.getString(9);
    }
    public Long getCodeId() {
        return super.getLong(10);
    }
    public String getProblemUGuid() {
        return super.getString(11);
    }
    public String getConsultationGuid() {
        return super.getString(12);
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
    public String getDocumentGuid() {
        return super.getString(21);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(22);
    }
}
