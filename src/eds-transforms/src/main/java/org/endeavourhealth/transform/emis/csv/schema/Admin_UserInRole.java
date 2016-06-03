package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Admin_UserInRole extends AbstractCsvTransformer {

    public Admin_UserInRole(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }

    public UUID getUserInRoleGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(1);
    }
    public String getTitle() {
        return super.getString(2);
    }
    public String getGivenName() {
        return super.getString(3);
    }
    public String getSurname() {
        return super.getString(4);
    }
    public Date getContractStartDate() throws TransformException {
        return super.getDate(5);
    }
    public Date getContractEndDate() throws TransformException {
        return super.getDate(6);
    }
    public String getJobCategoryCode() {
        return super.getString(7);
    }
    public String getJobCategoryName() {
        return super.getString(8);
    }
    public Integer getProcessingId() {
        return super.getInt(9);
    }
}
