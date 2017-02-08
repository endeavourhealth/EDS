package org.endeavourhealth.transform.emis.csv.schema.careRecord;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;

import java.io.File;
import java.util.Date;

public class Consultation extends AbstractCsvParser {

    public Consultation(String version, File f, boolean openParser) throws Exception {
        super(version, f, openParser, EmisCsvToFhirTransformer.CSV_FORMAT, EmisCsvToFhirTransformer.DATE_FORMAT_YYYY_MM_DD, EmisCsvToFhirTransformer.TIME_FORMAT);
    }

    @Override
    protected String[] getCsvHeaders(String version) {

        if (version.equals(EmisCsvToFhirTransformer.VERSION_5_0)) {
            return new String[]{
                    "ConsultationGuid",
                    "PatientGuid",
                    "OrganisationGuid",
                    "EffectiveDate",
                    "EffectiveDatePrecision",
                    "EnteredDate",
                    //"EnteredTime", //the earliest version (we've seen) doesn't have this column
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
        } else {
            return new String[]{
                    "ConsultationGuid",
                    "PatientGuid",
                    "OrganisationGuid",
                    "EffectiveDate",
                    "EffectiveDatePrecision",
                    "EnteredDate",
                    "EnteredTime",
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
    public Date getEnteredDate() throws TransformException {
        return super.getDate("EnteredDate");
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
