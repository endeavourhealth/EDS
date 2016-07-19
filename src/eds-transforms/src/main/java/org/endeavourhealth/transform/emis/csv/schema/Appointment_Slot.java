package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Appointment_Slot extends AbstractCsvTransformer {

    public Appointment_Slot(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "SlotGuid",
                "AppointmentDate",
                "AppointmentStartTime",
                "PlannedDurationInMinutes",
                "PatientGuid",
                "SendInTime",
                "LeftTime",
                "DidNotAttend",
                "PatientWaitInMin",
                "AppointmentDelayInMin",
                "ActualDurationInMinutes",
                "OrganisationGuid",
                "SessionGuid",
                "DnaReasonCodeId",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getSlotGuid() {
        return super.getString("SlotGuid");
    }
    public Date getAppointmentStartDateTime() throws TransformException {
        return super.getDateTime("AppointmentDate", "AppointmentStartTime");
    }
    public Integer getPlannedDurationInMinutes() {
        return super.getInt("PlannedDurationInMinutes");
    }
    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public Date getSendInDateTime() throws TransformException {
        return super.getDateTime("AppointmentDate", "SendInTime");
    }
    public Date getLeftDateTime() throws TransformException {
        return super.getDateTime("AppointmentDate", "LeftTime");
    }
    public boolean getDidNotAttend() {
        return super.getBoolean("DidNotAttend");
    }
    public Integer getPatientWaitInMin() {
        return super.getInt("PatientWaitInMin");
    }
    public Integer getAppointmentDelayInMin() {
        return super.getInt("AppointmentDelayInMin");
    }
    public Integer getActualDurationInMinutes() {
        return super.getInt("ActualDurationInMinutes");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public String getSessionGuid() {
        return super.getString("SessionGuid");
    }
    public Long getDnaReasonCodeId() {
        return super.getLong("DnaReasonCodeId");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
