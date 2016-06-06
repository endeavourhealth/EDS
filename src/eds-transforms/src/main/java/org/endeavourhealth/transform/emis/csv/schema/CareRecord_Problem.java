package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CareRecord_Problem extends AbstractCsvTransformer {

    public CareRecord_Problem(String folderPath, CSVFormat csvFormat) throws IOException {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }


    public String getObservationGuid() {
        return super.getString(0);
    }
    public String getParentProblemObservationGuid() {
        return super.getString(1);
    }
    public String getPatientGuid() {
        return super.getString(2);
    }
    public String getOrganisationGuid() {
        return super.getString(3);
    }
    public Date getEndDate() throws TransformException {
        return super.getDate(4);
    }
    public String getEffectiveDatePrecision() {
        return super.getString(5);
    }
    public Date getLastReviewDate() throws TransformException {
        return super.getDate(6);
    }
    public String getLastReviewDatePrecision() {
        return super.getString(7);
    }
    public String getLastReviewUserInRoleGuid() {
        return super.getString(8);
    }
    public Integer getExpectedDuration() {
        return super.getInt(9);
    }
    public String getSignificanceDescription() {
        return super.getString(10);
    }
    public String getProblemStatusDescription() {
        return super.getString(11);
    }
    public String getParentProblemRelationship() {
        return super.getString(12);
    }
    public String getComment() {
        return super.getString(13);
    }
    public Integer getProcessingId() {
        return super.getInt(14);
    }

}
