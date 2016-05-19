package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class AdminLocation extends AbstractCsvTransformer {


    public AdminLocation(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public String getHouseNameFlatNumber() {
        return super.getString(0);
    }
    public String getNumberAndStreet() {
        return super.getString(1);
    }
    public String getVillage() {
        return super.getString(2);
    }
    public String getTown() {
        return super.getString(3);
    }
    public String getCounty() {
        return super.getString(4);
    }
    public String getPostcode() {
        return super.getString(5);
    }
    public UUID getLocationGuid() {
        return super.getUniqueIdentifier(6);
    }
    public UUID getParentLocationId() {
        return super.getUniqueIdentifier(7);
    }
    public String getLocationName() {
        return super.getString(8);
    }
    public String getMainContactName() {
        return super.getString(10);
    }
    public String getEmailAddress() {
        return super.getString(11);
    }
    public String getPhoneNumber() {
        return super.getString(12);
    }
    public String getFaxNumber() {
        return super.getString(13);
    }
    public Date getOpenDate() throws TransformException {
        return super.getDate(14);
    }
    public Date getCloseDate() throws TransformException {
        return super.getDate(15);
    }
    public Integer getProcessingId() {
        return super.getInt(16);
    }
    public boolean getDeleted() {
        return super.getBoolean(17);
    }
    public String getLocationTypeDescription() {
        return super.getString(18);
    }




}