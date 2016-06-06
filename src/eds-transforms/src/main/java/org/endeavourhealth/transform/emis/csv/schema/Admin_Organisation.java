package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Admin_Organisation extends AbstractCsvTransformer {

    public Admin_Organisation(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public String getOrganisationGuid() {
        return super.getString(0);
    }
    public Integer getCDB() {
        return super.getInt(1);
    }
    public String getOrganisatioName() {
        return super.getString(2);
    }
    public String getODScode() {
        return super.getString(3);
    }
    public String getParentOrganisationGuid() {
        return super.getString(4);
    }
    public String getCCGOrganisationGuid() {
        return super.getString(5);
    }
    public String getOrganisationType() {
        return super.getString(6);
    }
    public Date getOpenDate() throws TransformException {
        return super.getDate(7);
    }
    public Date getCloseDate() throws TransformException {
        return super.getDate(8);
    }
    public String getMainLocationGuid() {
        return super.getString(9);
    }
    public Integer getProcessingId() {
        return super.getInt(10);
    }
}
