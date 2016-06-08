package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Appointment_Session extends AbstractCsvTransformer {

    public Appointment_Session(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "OrganisationGuid",
                "AppointmentSessionGuid",
                "Description",
                "SessionTypeDescription",
                "SessionCategoryDisplayName",
                "StartDate",
                "StartTime",
                "EndDate",
                "EndTime",
                "Private",
                "Deleted",
                "ProcessingId",
                "LocationGuid"
        };
    }

    public String getOrganisationGuid() {
        return super.getString(0);
    }
    public String getAppointmnetSessionGuid() {
        return super.getString(1);
    }
    public String getDescription() {
        return super.getString(2);
    }
    public String getSessionTypeDescription() {
        return super.getString(3);
    }
    public String getSessionCategoryDisplayName() {
        return super.getString(4);
    }
    public Date getStartDateTime() throws TransformException {
        return super.getDateTime(5, 6);
    }
    public Date getEndDateTime() throws TransformException {
        return super.getDateTime(7, 8);
    }
    public boolean getPrivate() {
        return super.getBoolean(9);
    }
    public boolean getDeleted() {
        return super.getBoolean(10);
    }
    public Integer getProcessingId() {
        return super.getInt(11);
    }
    public String getLocationGuid() {
        return super.getString(12);
    }
}
