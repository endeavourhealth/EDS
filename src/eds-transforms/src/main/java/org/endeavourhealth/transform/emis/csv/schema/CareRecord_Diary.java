package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class CareRecord_Diary extends AbstractCsvTransformer {

    public CareRecord_Diary(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "DiaryGuid",
                "PatientGuid",
                "OrganisationGuid",
                "EffectiveDate",
                "EffectiveDatePrecision",
                "EnteredDate",
                "EnteredTime",
                "ClinicianUserInRoleGuid",
                "EnteredByUserInRoleGuid",
                "CodeId",
                "OriginalTerm",
                "AssociatedText",
                "DurationTerm",
                "LocationTypeDescription",
                "Deleted",
                "IsConfidential",
                "IsActive",
                "IsComplete",
                "ConsultationGuid",
                "ProcessingId"

        };
    }

    public String getDiaryGuid() {
        return super.getString("DiaryGuid");
    }
    public String getPatientGuid() {
        return super.getString("PatientGuid");
    }
    public String getOrganisationGuid() {
        return super.getString("OrganisationGuid");
    }
    public Date getEffectiveDate() throws TransformException {
        return super.getDate("EffectiveDate");
    }
    public String getEffectiveDatePrecision() {
        return super.getString("EffectiveDatePrecision");
    }
    public Date getEnteredDateTime() throws TransformException {
        return super.getDateTime("EnteredDate", "EnteredTime");
    }
    public String getClinicianUserInRoleGuid() {
        return super.getString("ClinicianUserInRoleGuid");
    }
    public String getEnteredByUserInRoleGuid() {
        return super.getString("EnteredByUserInRoleGuid");
    }
    public Long getCodeId() {
        return super.getLong("CodeId");
    }
    public String getOriginalTerm() {
        return super.getString("OriginalTerm");
    }
    public String getAssociatedText() {
        return super.getString("AssociatedText");
    }
    public String getDurationTerm() {
        return super.getString("DurationTerm");
    }
    public String getLocationTypeDescription() {
        return super.getString("LocationTypeDescription");
    }
    public String getConsultationGuid() {
        return super.getString("ConsultationGuid");
    }
    public boolean getIsConfidential() {
        return super.getBoolean("IsConfidential");
    }
    public boolean getIsActive() {
        return super.getBoolean("IsActive");
    }
    public boolean getIsComplete() {
        return super.getBoolean("IsComplete");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }

}
