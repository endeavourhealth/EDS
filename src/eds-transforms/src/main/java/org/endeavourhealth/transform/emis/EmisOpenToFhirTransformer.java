package org.endeavourhealth.transform.emis;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.utility.XmlHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions.AppointmentSessionList;
import org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments.PatientAppointmentList;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession.SlotListStruct;
import org.endeavourhealth.transform.emis.emisopen.transforms.admin.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.clinical.*;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.Schedule;
import org.hl7.fhir.instance.model.Slot;

import java.util.ArrayList;
import java.util.List;

public final class EmisOpenToFhirTransformer
{
    public static List<Resource> toFhirFullRecord(String eomMedicalRecord38Xml) throws TransformException
    {
        MedicalRecordType medicalRecord = XmlHelper.deserialize(eomMedicalRecord38Xml, MedicalRecordType.class);

        String organisationGuid = null;
        if (medicalRecord.getOriginator() != null) {
            if (medicalRecord.getOriginator().getOrganisation() != null) {
                organisationGuid = medicalRecord.getOriginator().getOrganisation().getGUID();
            }
        }

        if (StringUtils.isBlank(organisationGuid)) {
            throw new TransformException("Could not determine current organisation guid");
        }

        String patientGuid = null;
        if (medicalRecord.getRegistration() != null) {
            patientGuid = medicalRecord.getRegistration().getGUID();
        }
        if (Strings.isNullOrEmpty(patientGuid)) {
            throw new TransformException("No patient GUID in EmisOpen record");
        }

        List<Resource> result = new ArrayList<>();

        OrganizationTransformer.transform(medicalRecord, result);
        LocationTransformer.transform(medicalRecord, result);
        PractitionerTransformer.transform(medicalRecord, organisationGuid, result);

        PatientTransformer.transform(medicalRecord, result, organisationGuid, patientGuid);
        EpisodeOfCareTransformer.transform(medicalRecord, result, organisationGuid, patientGuid);

        ProblemTransformer.transform(medicalRecord, result, organisationGuid, patientGuid);
        EventTransformer.transform(medicalRecord, result, patientGuid);
        IssueTransformer.transform(medicalRecord, result, patientGuid);
        MedicationTransformer.transform(medicalRecord, result, patientGuid);
        AllergyTransformer.transform(medicalRecord, result, patientGuid);
        ConsultationTransformer.transform(medicalRecord, result, patientGuid);
        DiaryTransformer.transform(medicalRecord, result, patientGuid);
        ReferralTransformer.transform(medicalRecord, result, patientGuid);
        AppointmentTransformer.transform(medicalRecord, result, patientGuid);
        InvestigationTransformer.transform(medicalRecord, result, patientGuid);
        TestRequestHeaderTransformer.transform(medicalRecord, result, patientGuid);

        return result;
    }

    public static List<Schedule> toFhirSchedules(String eopenSchedulesXml) throws TransformException
    {
        AppointmentSessionList appointmentSessionList = XmlHelper.deserialize(eopenSchedulesXml, AppointmentSessionList.class);
        return ScheduleTransformer.transform(appointmentSessionList);
    }

    public static List<Slot> toFhirSlots(String eopenSlotsXml) throws TransformException
    {
        SlotListStruct slotListStruct = XmlHelper.deserialize(eopenSlotsXml, SlotListStruct.class);
        return SlotTransformer.transform(slotListStruct);
    }

    public static List<Appointment> toFhirAppointments(String patientId, String eopenAppointmentsXml) throws TransformException
    {
        PatientAppointmentList patientAppointmentList = XmlHelper.deserialize(eopenAppointmentsXml, PatientAppointmentList.class);
        return AppointmentTransformer.transform(patientId, patientAppointmentList);
    }
}
