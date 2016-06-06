package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.UUID;

public class Admin_OrganisationLocation extends AbstractCsvTransformer {

    public Admin_OrganisationLocation(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public String getOrgansationGuid() {
        return super.getString(0);
    }
    public String getLocationGuid() {
        return super.getString(1);
    }
    public boolean getIsMainLocation() {
        return super.getBoolean(2);
    }
    public boolean getDeleted() {
        return super.getBoolean(3);
    }
    public Integer getProcessingId() {
        return super.getInt(4);
    }
}
