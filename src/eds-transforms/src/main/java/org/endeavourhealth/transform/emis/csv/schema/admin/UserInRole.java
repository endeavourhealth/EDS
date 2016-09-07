package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.util.Date;

public class UserInRole extends AbstractCsvTransformer {

    public UserInRole(String version, String folderPath, CSVFormat csvFormat) throws Exception {
        super(version, folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
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
