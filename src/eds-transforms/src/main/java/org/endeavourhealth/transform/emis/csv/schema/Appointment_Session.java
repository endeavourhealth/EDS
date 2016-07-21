package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Appointment_Session extends AbstractCsvTransformer {

    public Appointment_Session(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "AppointmentSessionGuid",
                "Description",
                "LocationGuid",
                "SessionTypeDescription",
                "SessionCategoryDisplayName",
                "StartDate",
                "StartTime",
                "EndDate",
                "EndTime",
                "Private",
                "OrganisationGuid",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getAppointmnetSessionGuid() {
        return super.getString("AppointmentSessionGuid");
    }
    public String getDescription() {
        return super.getString("Description");
    }
    public String getLocationGuid() {
        return super.getString("LocationGuid");
    }
    public String getSessionTypeDescription() {
        return super.getString("SessionTypeDescription");
    }
    public String getSessionCategoryDisplayName() {
        return super.getString("SessionCategoryDisplayName");
    }
    public Date getStartDateTime() throws TransformException {
        return super.getDateTime("StartDate", "StartTime");
    }
    public Date getEndDateTime() throws TransformException {
        return super.getDateTime("EndDate", "EndTime");
    }
    public boolean getPrivate() {
        return super.getBoolean("Private");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }

}
