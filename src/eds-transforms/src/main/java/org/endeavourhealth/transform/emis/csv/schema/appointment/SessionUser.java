package org.endeavourhealth.transform.emis.csv.schema.appointment;

import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.io.File;

public class SessionUser extends AbstractCsvTransformer {

    public SessionUser(String version, File f) throws Exception {
        super(version, f, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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
