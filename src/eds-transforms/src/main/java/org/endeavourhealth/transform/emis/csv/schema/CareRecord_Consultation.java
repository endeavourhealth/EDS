package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.util.Date;

public class CareRecord_Consultation extends AbstractCsvTransformer {

    public CareRecord_Consultation(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[]{
                "ConsultationGuid",
                "PatientGuid",
                "OrganisationGuid",
                "EffectiveDate",
                "EffectiveDatePrecision",
                "EnteredDate",
                //"EnteredTime", //spec.states this column is present, but test data doesn't contain it
                "ClinicianUserInRoleGuid",
                "EnteredByUserInRoleGuid",
                "AppointmentSlotGuid",
                "ConsultationSourceTerm",
                "ConsultationSourceCodeId",
                "Complete",
                "Deleted",
                "IsConfidential",
                "ProcessingId"
        };
    }

    public String getConsultationGuid() {
        return super.getString("ConsultationGuid");
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
    public String getAppointmentSlotGuid() {
        return super.getString("AppointmentSlotGuid");
    }
    public String getConsultationSourceTerm() {
        return super.getString("ConsultationSourceTerm");
    }
    public boolean getComplete() {
        return super.getBoolean("Complete");
    }
    public boolean getDeleted() {
        return super.getBoolean("Deleted");
    }
    public Long getConsultationSourceCodeId() {
        return super.getLong("ConsultationSourceCodeId");
    }
    public Integer getProcessingId() {
        return super.getInt("ProcessingId");
    }
    public boolean getIsConfidential() {
        return super.getBoolean("IsConfidential");
    }

}
