package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class CareRecordDiary extends AbstractCsvTransformer {
    public CareRecordDiary(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getDiaryGuid() {
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
    public String getOriginalTerm() {
        return super.getString(10);
    }
    public String getAssociatedText() {
        return super.getString(11);
    }
    public String getDurationTerm() {
        return super.getString(12);
    }
    public String getLocationTypeDescription() {
        return super.getString(13);
    }
    public UUID getConsultationGuid() {
        return super.getUniqueIdentifier(14);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(15);
    }
    public boolean getIsActive() {
        return super.getBoolean(16);
    }
    public boolean getIsComplete() {
        return super.getBoolean(17);
    }
    public boolean getDeleted() {
        return super.getBoolean(18);
    }
    public Integer getProcessingId() {
        return super.getInt(19);
    }

}
