package org.endeavourhealth.transform.emis.csv.schema.appointment;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class Session extends AbstractCsvParser {

    public Session(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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
