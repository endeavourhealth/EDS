package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;

import java.util.UUID;

public class Admin_OrganisationLocation extends AbstractCsvTransformer {


    public Admin_OrganisationLocation(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getOrgansationGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getLocationGuid() {
        return super.getUniqueIdentifier(1);
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
