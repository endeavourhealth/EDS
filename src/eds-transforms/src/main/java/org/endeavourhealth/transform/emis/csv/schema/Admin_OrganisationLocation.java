package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

public class Admin_OrganisationLocation extends AbstractCsvTransformer {

    public Admin_OrganisationLocation(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "OrganisationGuid",
                "LocationGuid",
                "IsMainLocation",
                "Deleted",
                "ProcessingId"
        };
    }

    public String getOrgansationGuid() {
        return super.getString("OrganisationGuid");
    }
    public String getLocationGuid() {
        return super.getString("LocationGuid");
    }
    public boolean getIsMainLocation() {
        return super.getBoolean("IsMainLocation");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
