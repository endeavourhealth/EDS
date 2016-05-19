package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;

import java.util.Date;
import java.util.UUID;

public class CareRecord_Problem extends AbstractCsvTransformer {
    public CareRecord_Problem(CSVParser csvReader, String dateFormat, String timeFormat) {
        super(csvReader, dateFormat, timeFormat);
    }

    public UUID getObservationGuid() {
        return super.getUniqueIdentifier(0);
    }
    public UUID getParentProblemObservationGuid() {
        return super.getUniqueIdentifier(1);
    }
    public UUID getPatientGuid() {
        return super.getUniqueIdentifier(2);
    }
    public UUID getOrganisationGuid() {
        return super.getUniqueIdentifier(3);
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
    public UUID getLastReviewUserInRoleGuid() {
        return super.getUniqueIdentifier(8);
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
