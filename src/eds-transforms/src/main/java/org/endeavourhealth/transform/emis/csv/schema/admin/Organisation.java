package org.endeavourhealth.transform.emis.csv.schema.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.io.File;
import java.util.Date;

public class Organisation extends AbstractCsvTransformer {

    public Organisation(String version, File f) throws Exception {
        super(version, f, EmisCsvTransformer.CSV_FORMAT, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "OrganisationGuid",
                "CDB",
                "OrganisationName",
                "ODSCode",
                "ParentOrganisationGuid",
                "CCGOrganisationGuid",
                "OrganisationType",
                "OpenDate",
                "CloseDate",
                "MainLocationGuid",
                "ProcessingId"
        };
    }

    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public Integer getCDB() {
        return super.getInt("CDB");
    }
    public String getOrganisatioName() {
        return super.getString("OrganisationName");
    }
    public String getODScode() {
        return super.getString("ODSCode");
    }
    public String getParentOrganisationGuid() {
        return super.getString("ParentOrganisationGuid");
    }
    public String getCCGOrganisationGuid() {
        return super.getString("CCGOrganisationGuid");
    }
    public String getOrganisationType() {
        return super.getString("OrganisationType");
    }
    public Date getOpenDate() throws TransformException {
        return super.getDate("OpenDate");
    }
    public Date getCloseDate() throws TransformException {
        return super.getDate("CloseDate");
    }
    public String getMainLocationGuid() {
        return super.getString("MainLocationGuid");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
