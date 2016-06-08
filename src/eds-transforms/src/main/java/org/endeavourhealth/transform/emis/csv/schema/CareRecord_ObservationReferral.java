package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CareRecord_ObservationReferral extends AbstractCsvTransformer {

    public CareRecord_ObservationReferral(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
    }


    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "ObservationGuid",
                "PatientGuid",
                "OrganisationGuid",
                "ReferralTargetOrganisationGuid",
                "ReferralUrgency",
                "ReferralMode",
                "ReferralServiceType",
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
        return super.getString(0);
    }
    public String getPatientGuid() {
        return super.getString(1);
    }
    public String getOrganisationGuid() {
        return super.getString(2);
    }
    public String getReferalTargetOrganisationGuid() {
        return super.getString(3);
    }
    public String getReferralUrgency() {
        return super.getString(4);
    }
    public String getReferralMode() {
        return super.getString(5);
    }
    public String getReferralServiceType() {
        return super.getString(6);
    }
    public Date getReferralReceivedDateTime() throws TransformException {
        return super.getDateTime(7, 8);
    }
    public Date getReferralEndDate() throws TransformException {
        return super.getDate(9);
    }
    public Long getReferralSourceId() {
        return super.getLong(10);
    }
    public String getReferralSourceOrganisationGuid() {
        return super.getString(11);
    }
    public String getReferralUBRN() {
        return super.getString(12);
    }
    public Long getReferralReasonCodeId() {
        return super.getLong(13);
    }
    public Long getReferringCareProfessionalStaffGroupCodeId() {
        return super.getLong(14);
    }
    public Long getReferralEpisodeRTTMeasurmentTypeId() {
        return super.getLong(15);
    }
    public Date getReferralEpisodeClosureDate() throws TransformException {
        return super.getDate(16);
    }
    public Date getReferralEpisideDischargeLetterIssuedDate() throws TransformException {
        return super.getDate(17);
    }
    public Long getReferralClosureReasonCodeId() {
        return super.getLong(18);
    }
    public Integer getProcessingId() {
        return super.getInt(19);
    }
}
