package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;

import java.util.UUID;

public class AppointmentSessionUser extends AbstractCsvTransformer {
    public AppointmentSessionUser(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getSessionGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getUserInRoleGuid() {
        return super.getUniqueIdentifier(1);
    }
    public boolean getdDeleted() {
        return super.getBoolean(2);
    }
    public int getProcessingId() {
        return super.getInt(3);
    }
}
