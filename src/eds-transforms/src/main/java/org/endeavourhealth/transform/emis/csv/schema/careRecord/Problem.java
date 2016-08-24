package org.endeavourhealth.transform.emis.csv.schema.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.util.Date;

public class Problem extends AbstractCsvTransformer {

    public Problem(String version, String folderPath, CSVFormat csvFormat) throws Exception {
        super(version, folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {
        return new String[]{
                "ObservationGuid",
                "PatientGuid",
                "OrganisationGuid",
                "ParentProblemObservationGuid",
                "Comment",
                "EndDate",
                "EndDatePrecision",
                "ExpectedDuration",
                "LastReviewDate",
                "LastReviewDatePrecision",
                "LastReviewUserInRoleGuid",
                "ParentProblemRelationship",
                "ProblemStatusDescription",
                "SignificanceDescription",
                "ProcessingId"
        };
    }

    public String getObservationGuid() {
        return super.getString("ObservationGuid");
    }
    public String getParentProblemObservationGuid() {
        return super.getString("ParentProblemObservationGuid");
    }
    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public Date getEndDate() throws TransformException {
        return super.getDate("EndDate");
    }
    public String getEndDatePrecision() {
        return super.getString("EndDatePrecision");
    }
    public Date getLastReviewDate() throws TransformException {
        return super.getDate("LastReviewDate");
    }
    public String getLastReviewDatePrecision() {
        return super.getString("LastReviewDatePrecision");
    }
    public String getLastReviewUserInRoleGuid() {
        return super.getString("LastReviewUserInRoleGuid");
    }
    public Integer getExpectedDuration() {
        return super.getInt("ExpectedDuration");
    }
    public String getSignificanceDescription() {
        return super.getString("SignificanceDescription");
    }
    public String getProblemStatusDescription() {
        return super.getString("ProblemStatusDescription");
    }
    public String getParentProblemRelationship() {
        return super.getString("ParentProblemRelationship");
    }
    public String getComment() {
        return super.getString("Comment");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }

}
