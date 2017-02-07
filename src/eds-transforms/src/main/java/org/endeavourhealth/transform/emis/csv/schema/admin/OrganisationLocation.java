package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;

public class OrganisationLocation extends AbstractCsvParser {

    public OrganisationLocation(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
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
