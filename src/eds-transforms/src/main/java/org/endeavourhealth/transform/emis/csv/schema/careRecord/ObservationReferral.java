package org.endeavourhealth.transform.emis.csv.schema.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvTransformerWorker;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;

import java.util.Date;

public class ObservationReferral extends AbstractCsvTransformer {

    public ObservationReferral(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformerWorker.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformerWorker.TIME_FORMAT);
    }


    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "ObservationGuid",
                "PatientGuid",
                "OrganisationGuid",
                "ReferralTargetOrganisationGuid",
                "ReferralUrgency",
                "ReferralServiceType",
                "ReferralMode",
                "ReferralReceivedDate",
                "ReferralReceivedTime",
                "ReferralEndDate",
                "ReferralSourceId",
                "ReferralSourceOrganisationGuid",
                "ReferralUBRN",
                "ReferralReasonCodeId",
                "ReferringCareProfessionalStaffGroupCodeId",
                "ReferralEpisodeRTTMeasurementTypeId",
                "ReferralEpisodeClosureDate",
                "ReferralEpisodeDischargeLetterIssuedDate",
                "ReferralClosureReasonCodeId",
                "ProcessingId"
        };
    }

    public String getObservationGuid() {
        return super.getString("ObservationGuid");
    }
    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public String getReferalTargetOrganisationGuid() {
        return super.getString("ReferralTargetOrganisationGuid");
    }
    public String getReferralUrgency() {
        return super.getString("ReferralUrgency");
    }
    public String getReferralMode() {
        return super.getString("ReferralMode");
    }
    public String getReferralServiceType() {
        return super.getString("ReferralServiceType");
    }
    public Date getReferralReceivedDateTime() throws TransformException {
        return super.getDateTime("ReferralReceivedDate", "ReferralReceivedTime");
    }
    public Date getReferralEndDate() throws TransformException {
        return super.getDate("ReferralEndDate");
    }
    public Long getReferralSourceId() {
        return super.getLong("ReferralSourceId");
    }
    public String getReferralSourceOrganisationGuid() {
        return super.getString("ReferralSourceOrganisationGuid");
    }
    public String getReferralUBRN() {
        return super.getString("ReferralUBRN");
    }
    public Long getReferralReasonCodeId() {
        return super.getLong("ReferralReasonCodeId");
    }
    public Long getReferringCareProfessionalStaffGroupCodeId() {
        return super.getLong("ReferringCareProfessionalStaffGroupCodeId");
    }
    public Long getReferralEpisodeRTTMeasurmentTypeId() {
        return super.getLong("ReferralEpisodeRTTMeasurementTypeId");
    }
    public Date getReferralEpisodeClosureDate() throws TransformException {
        return super.getDate("ReferralEpisodeClosureDate");
    }
    public Date getReferralEpisideDischargeLetterIssuedDate() throws TransformException {
        return super.getDate("ReferralEpisodeDischargeLetterIssuedDate");
    }
    public Long getReferralClosureReasonCodeId() {
        return super.getLong("ReferralClosureReasonCodeId");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
}
