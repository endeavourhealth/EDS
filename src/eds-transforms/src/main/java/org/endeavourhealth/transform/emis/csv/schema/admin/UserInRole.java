package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class UserInRole extends AbstractCsvParser {

    public UserInRole(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "UserInRoleGuid",
                "OrganisationGuid",
                "Title",
                "GivenName",
                "Surname",
                "JobCategoryCode",
                "JobCategoryName",
                "ContractStartDate",
                "ContractEndDate",
                "ProcessingId"
        };
    }

    public String getUserInRoleGuid() {
        return super.getString("UserInRoleGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public String getTitle() {
        return super.getString("Title");
    }
    public String getGivenName() {
        return super.getString("GivenName");
    }
    public String getSurname() {
        return super.getString("Surname");
    }
    public String getJobCategoryCode() {
        return super.getString("JobCategoryCode");
    }
    public String getJobCategoryName() {
        return super.getString("JobCategoryName");
    }
    public Date getContractStartDate() throws TransformException {
        return super.getDate("ContractStartDate");
    }
    public Date getContractEndDate() throws TransformException {
        return super.getDate("ContractEndDate");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
