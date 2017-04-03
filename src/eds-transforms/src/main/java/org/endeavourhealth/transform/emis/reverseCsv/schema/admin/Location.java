package org.endeavourhealth.transform.emis.reverseCsv.schema.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

public class Location extends AbstractCsvWriter {

    public Location(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeLine(String locationGuid,
                          String locationName,
                          String locationTypeDescription,
                          String parentLocationGuid,
                          String openDate,
                          String closeDate,
                          String mainContactName,
                          String faxNumber,
                          String emailAddress,
                          String phoneNumber,
                          String houseNameFlatNumber,
                          String numberAndStreet,
                          String village,
                          String town,
                          String county,
                          String postcode,
                          String deleted,
                          String processingId) {

    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "LocationGuid",
                "LocationName",
                "LocationTypeDescription",
                "ParentLocationGuid",
                "OpenDate",
                "CloseDate",
                "MainContactName",
                "FaxNumber",
                "EmailAddress",
                "PhoneNumber",
                "HouseNameFlatNumber",
                "NumberAndStreet",
                "Village",
                "Town",
                "County",
                "Postcode",
                "Deleted",
                "ProcessingId"
        };
    }
}
