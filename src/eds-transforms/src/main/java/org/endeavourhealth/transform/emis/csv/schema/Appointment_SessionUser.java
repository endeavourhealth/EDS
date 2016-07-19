package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

public class Appointment_SessionUser extends AbstractCsvTransformer {

    public Appointment_SessionUser(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "SessionGuid",
                "UserInRoleGuid",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getSessionGuid() {
        return super.getString("SessionGuid");
    }
    public String getUserInRoleGuid() {
        return super.getString("UserInRoleGuid");
    }
    public boolean getdDeleted() {
        return super.getBoolean("Deleted");
    }
    public int getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
