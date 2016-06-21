package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Audit_PatientAudit extends AbstractCsvTransformer {

    public Audit_PatientAudit(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
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
        return getString(0);
    }
    public String getPatientGuid() {
        return getString(1);
    }
    public String getOrganisationGuid() {
        return getString(2);
    }
    public Date getModifiedDateTime() throws TransformException {
        return getDateTime(3, 4);
    }
    public String getUserInRoleGuid() {
        return getString(5);
    }
    public String getItemType() {
        return getString(6);
    }
    public String getModeType() {
        return getString(7);
    }
    public Integer getProcessingId() {
        return getInt(8);
    }
}
