package org.endeavourhealth.transform.emis.csv.schema.audit;

import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class PatientAudit extends AbstractCsvParser {

    public PatientAudit(String version, File f) throws Exception {
        super(version, f, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "ItemGuid",
                "PatientGuid",
                "OrganisationGuid",
                "ModifiedDate",
                "ModifiedTime",
                "UserInRoleGuid",
                "ItemType",
                "ModeType",
                "ProcessingId"
        };
    }

    public String getItemGuid() {
        return getString("ItemGuid");
    }

    public String getPatientGuid() {
        return getString("PatientGuid");
    }

    public String getOrganisationGuid() {
        return getString("OrganisationGuid");
    }

    public Date getModifiedDateTime() throws Exception {
        return getDateTime("ModifiedDate", "ModifiedTime");
    }

    public String getUserInRoleGuid() {
        return getString("UserInRoleGuid");
    }

    public String getItemType() {
        return getString("ItemType");
    }

    public String getModeType() {
        return getString("ModeType");
    }

    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }


}
