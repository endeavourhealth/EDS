package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class Location extends AbstractCsvParser {

    public Location(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
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

    public String getLocationGuid() {
        return super.getString("LocationGuid");
    }
    public String getLocationName() {
        return super.getString("LocationName");
    }
    public String getLocationTypeDescription() {
        return super.getString("LocationTypeDescription");
    }
    public String getParentLocationId() {
        return super.getString("ParentLocationGuid");
    }
    public Date getOpenDate() throws TransformException {
        return super.getDate("OpenDate");
    }
    public Date getCloseDate() throws TransformException {
        return super.getDate("CloseDate");
    }
    public String getMainContactName() {
        return super.getString("MainContactName");
    }
    public String getFaxNumber() {
        return super.getString("FaxNumber");
    }
    public String getEmailAddress() {
        return super.getString("EmailAddress");
    }
    public String getPhoneNumber() {
        return super.getString("PhoneNumber");
    }
    public String getHouseNameFlatNumber() {
        return super.getString("HouseNameFlatNumber");
    }
    public String getNumberAndStreet() {
        return super.getString("NumberAndStreet");
    }
    public String getVillage() {
        return super.getString("Village");
    }
    public String getTown() {
        return super.getString("Town");
    }
    public String getCounty() {
        return super.getString("County");
    }
    public String getPostcode() {
        return super.getString("Postcode");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }





}