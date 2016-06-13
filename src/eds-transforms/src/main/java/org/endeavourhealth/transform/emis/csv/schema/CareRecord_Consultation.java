package org.endeavourhealth.transform.emis.csv.schema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CareRecord_Consultation extends AbstractCsvTransformer {

    public CareRecord_Consultation(String folderPath, CSVFormat csvFormat) throws Exception {
        super(folderPath, csvFormat, EmisCsvTransformer.DATE_FORMAT, EmisCsvTransformer.TIME_FORMAT);
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
                "EnteredTime",
                "ClinicianUserInRoleGuid",
                "EnteredByUserInRoleGuid",
                "AppointmentSlotGuid",
                "ConsultationSourceTerm",
                "Complete",
                "Deleted",
                "ConsultationSourceCodeId",
                "ProcessingId",
                "IsConfidential"
        };
    }

    public String getConsultationGuid() {
        return super.getString(0);
    }
    public String getPatientGuid() {
        return super.getString(1);
    }
    public String getOrganisationGuid() {
        return super.getString(2);
    }
    public Date getEffectiveDate() throws TransformException {
        return super.getDate(3);
    }
    public String getEffectiveDatePrecision() {
        return super.getString(4);
    }
    public Date getEnteredDateTime() throws TransformException {
        return super.getDateTime(5, 6);
    }
    public String getClinicianUserInRoleGuid() {
        return super.getString(7);
    }
    public String getEnteredByUserInRoleGuid() {
        return super.getString(8);
    }
    public String getAppointmentSlotGuid() {
        return super.getString(9);
    }
    public String getConsultationSourceTerm() {
        return super.getString(10);
    }
    public boolean getComplete() {
        return super.getBoolean(11);
    }
    public boolean getDeleted() {
        return super.getBoolean(12);
    }
    public Long getConsultationSourceCodeId() {
        return super.getLong(13);
    }
    public Integer getProcessingId() {
        return super.getInt(14);
    }
    public boolean getIsConfidential() {
        return super.getBoolean(15);
    }

}
