package org.endeavourhealth.transform.emis.reverseCsv.schema.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

public class Session extends AbstractCsvWriter {

    public Session(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }
    
    public void writeLine(String appointmentSessionGuid,
                          String description,
                          String locationGuid,
                          String sessionTypeDescription,
                          String sessionCategoryDisplayName,
                          String startDate,
                          String startTime,
                          String endDate,
                          String endTime,
                          String isPrivate,
                          String organisationGuid,
                          String deleted,
                          String processingId) {

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
}
