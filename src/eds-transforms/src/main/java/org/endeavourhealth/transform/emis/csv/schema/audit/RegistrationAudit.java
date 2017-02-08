package org.endeavourhealth.transform.emis.csv.schema.audit;

import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class RegistrationAudit extends AbstractCsvParser {

    public RegistrationAudit(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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

    public String getModeType() {
        return getString("ModeType");
    }

    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }


}
