package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.UUID;

public class Appointment_SessionUser extends AbstractCsvTransformer {

    public Appointment_SessionUser(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public String getSessionGuid() {
        return super.getString(0);
    }
    public String getUserInRoleGuid() {
        return super.getString(1);
    }
    public boolean getdDeleted() {
        return super.getBoolean(2);
    }
    public int getProcessingId() {
        return super.getInt(3);
    }
}
