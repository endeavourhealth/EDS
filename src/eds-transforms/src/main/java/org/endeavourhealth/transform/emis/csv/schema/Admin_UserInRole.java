package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class Admin_UserInRole extends AbstractCsvTransformer {

    public Admin_UserInRole(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
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
