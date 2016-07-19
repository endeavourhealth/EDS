package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Agreements_SharingOrganisation extends AbstractCsvTransformer {

    public Agreements_SharingOrganisation(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "OrganisationGuid",
                "IsActivated",
                "Disabled",
                "Deleted",
                "LastModifiedDate"
        };
    }

    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public boolean getIsActivated() {
        return super.getBoolean("IsActivated");
    }
    public boolean getDisabled() {
        return super.getBoolean("Disabled");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Date getLastModifiedDate() throws TransformException {
        return super.getDate("LastModifiedDate");
    }
}
