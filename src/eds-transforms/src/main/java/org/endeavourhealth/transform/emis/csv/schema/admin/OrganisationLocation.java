package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.io.File;

public class OrganisationLocation extends AbstractCsvTransformer {

    public OrganisationLocation(String version, File f) throws Exception {
        super(version, f, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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
