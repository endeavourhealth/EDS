package org.endeavourhealth.transform.emis;

import org.endeavourhealth.core.utility.XmlHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions.AppointmentSessionList;
import org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments.PatientAppointmentList;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.schema.eomslotsforsession.SlotListStruct;
import org.endeavourhealth.transform.emis.emisopen.transforms.AppointmentTransformer;
import org.endeavourhealth.transform.emis.emisopen.transforms.PatientTransformer;
import org.endeavourhealth.transform.emis.emisopen.transforms.ScheduleTransformer;
import org.endeavourhealth.transform.emis.emisopen.transforms.SlotTransformer;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public final class EmisOpenTransformer
{
    public List<Resource> toFhirFullRecord(String eomMedicalRecord38Xml) throws TransformException
    {
        MedicalRecordType emisOpenMedicalRecord = XmlHelper.deserialize(eomMedicalRecord38Xml, MedicalRecordType.class);

        List<Resource> result = new ArrayList<>();

        result.add(PatientTransformer.transform(emisOpenMedicalRecord));

        return result;
    }

    public List<Schedule> toFhirSchedules(String eopenSchedulesXml) throws TransformException
    {
        AppointmentSessionList appointmentSessionList = XmlHelper.deserialize(eopenSchedulesXml, AppointmentSessionList.class);
        return ScheduleTransformer.transform(appointmentSessionList);
    }

    public List<Slot> toFhirSlots(String eopenSlotsXml) throws TransformException
    {
        SlotListStruct slotListStruct = XmlHelper.deserialize(eopenSlotsXml, SlotListStruct.class);
        return SlotTransformer.transform(slotListStruct);
    }

    public List<Appointment> toFhirAppointments(String patientId, String eopenAppointmentsXml) throws TransformException
    {
        PatientAppointmentList patientAppointmentList = XmlHelper.deserialize(eopenAppointmentsXml, PatientAppointmentList.class);
        return AppointmentTransformer.transform(patientId, patientAppointmentList);
    }
}
