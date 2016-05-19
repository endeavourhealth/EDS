package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class AppointmentSlot extends AbstractCsvTransformer {
    public AppointmentSlot(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getSlotGuid() {
        return super.getUniqueIdentifier(0);
    }
    public Date getAppointmentStartDateTime() throws TransformException {
        return super.getDateTime(1, 2);
    }
    public Integer getPlannedDurationInMinutes() {
        return super.getInt(3);
    }
    public UUID getPatientGuid() {
        return super.getUniqueIdentifier(4);
    }
    public Date getSendInDateTime() throws TransformException {
        return super.getDateTime(1, 5);
    }
    public Date getLeftDateTime() throws TransformException {
        return super.getDateTime(1, 6);
    }
    public boolean getDidNotAttend() {
        return super.getBoolean(7);
    }
    public Integer getPatientWaitInMin() {
        return super.getInt(8);
    }
    public Integer getAppointmentDelayInMin() {
        return super.getInt(9);
    }
    public Integer getActualDurationInMinutes() {
        return super.getInt(10);
    }
    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(11);
    }
    public UUID getSessionGuid() {
        return super.getUniqueIdentifier(12);
    }
    public Long getDnaReasonCodeId() {
        return super.getLong(13);
    }
    public boolean getDeleted() {
        return super.getBoolean(14);
    }
    public Integer getProcessingId() {
        return super.getInt(15);
    }
}
