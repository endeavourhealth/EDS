package org.endeavourhealth.transform.emis.csv.schema.agreements;

import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.io.File;
import java.util.Date;

public class SharingOrganisation extends AbstractCsvTransformer {

    public SharingOrganisation(String version, File f) throws Exception {
        super(version, f, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "OrganisationGuid",
                "IsActivated",
                "LastModifiedDate",
                "Disabled",
                "Deleted"
        };
    }

    public String getOrganisationGuid() {
        return getString("OrganisationGuid");
    }

    public boolean getIsActivated() {
        return getBoolean("IsActivated");
    }

    public Date getLastModifiedDate() throws Exception {
        return getDate("LastModifiedDate");
    }

    public boolean getDisabled() {
        return getBoolean("Disabled");
    }

    public boolean getDeleted() {
        return getBoolean("Deleted");
    }
}
