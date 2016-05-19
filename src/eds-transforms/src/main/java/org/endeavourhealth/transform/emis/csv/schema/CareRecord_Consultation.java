package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class CareRecord_Consultation extends AbstractCsvTransformer {

    public CareRecord_Consultation(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getConsultationGuid() {
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
    public UUID getAppointmentSlotGuid() {
        return super.getUniqueIdentifier(9);
    }
    public String getConsultationSourceTerm() {
        return super.getString(10);
    }
    public boolean getComplete() {
        return super.getBoolean(11);
    }
    public boolean getDeleted() {
        return super.getBoolean(12);
    }
    public Long getConsultationSourceCodeId() {
        return super.getLong(13);
    }
    public Integer getProcessingId() {
        return super.getInt(14);
    }
    public boolean isConfidential() {
        return super.getBoolean(15);
    }

}
