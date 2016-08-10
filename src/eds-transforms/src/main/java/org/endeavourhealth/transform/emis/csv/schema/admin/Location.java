package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.util.Date;

public class Location extends AbstractCsvTransformer {

    public Location(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
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