package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Audit_PatientAudit extends AbstractCsvTransformer {

    public Audit_PatientAudit(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
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
    public Date getModifiedDateTime() throws TransformException {
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
        return getInt("ProcessingId");
    }
}
