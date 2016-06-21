package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Audit_RegistrationAudit extends AbstractCsvTransformer {

    public Audit_RegistrationAudit(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "PatientGuid",
                "OrganisationGuid",
                "ModifiedDate",
                "ModifiedTime",
                "UserInRoleGuid",
                "ModeType",
                "ProcessingId"
        };
    }

    public String getPatientGuid() {
        return getString(0);
    }
    public String getOrganisationGuid() {
        return getString(1);
    }
    public Date getModifiedDateTime() throws TransformException {
        return getDateTime(2, 3);
    }
    public String getUserInRoleGuid() {
        return getString(4);
    }
    public String getModeType() {
        return getString(5);
    }
    public Integer getProcessingId() {
        return getInt(6);
    }
}
