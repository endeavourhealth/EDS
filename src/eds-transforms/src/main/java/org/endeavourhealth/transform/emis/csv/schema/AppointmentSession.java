package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class AppointmentSession extends AbstractCsvTransformer {


    public AppointmentSession(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getAppointmnetSessionGuid() {
        return super.getUniqueIdentifier(1);
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
    public UUID getLocationGuid() {
        return super.getUniqueIdentifier(12);
    }
}
