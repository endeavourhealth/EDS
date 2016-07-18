package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Admin_Location extends AbstractCsvTransformer {

    public Admin_Location(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
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

    public String getLocationGuid() {
        return super.getString(0);
    }
    public String getLocationName() {
        return super.getString(1);
    }
    public String getLocationTypeDescription() {
        return super.getString(2);
    }
    public String getParentLocationId() {
        return super.getString(3);
    }
    public Date getOpenDate() throws TransformException {
        return super.getDate(4);
    }
    public Date getCloseDate() throws TransformException {
        return super.getDate(5);
    }
    public String getMainContactName() {
        return super.getString(6);
    }
    public String getFaxNumber() {
        return super.getString(7);
    }
    public String getEmailAddress() {
        return super.getString(8);
    }
    public String getPhoneNumber() {
        return super.getString(9);
    }
    public String getHouseNameFlatNumber() {
        return super.getString(10);
    }
    public String getNumberAndStreet() {
        return super.getString(11);
    }
    public String getVillage() {
        return super.getString(12);
    }
    public String getTown() {
        return super.getString(13);
    }
    public String getCounty() {
        return super.getString(14);
    }
    public String getPostcode() {
        return super.getString(15);
    }
    public boolean getDeleted() {
        return super.getBoolean(16);
    }
    public Integer getProcessingId() {
        return super.getInt(17);
    }





}