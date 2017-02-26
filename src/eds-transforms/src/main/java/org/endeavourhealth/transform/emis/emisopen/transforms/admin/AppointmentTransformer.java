package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments.AppointmentStruct;
import org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments.HolderStruct;
import org.endeavourhealth.transform.emis.emisopen.schema.eomgetpatientappointments.PatientAppointmentList;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AppointmentListType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.AppointmentType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.MedicalRecordType;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentTransformer {

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        AppointmentListType appointmentList = medicalRecord.getAppointmentList();
        if (appointmentList == null) {
            return;
        }

        for (AppointmentType appointment : appointmentList.getAppointment()) {
            Resource resource = transform(appointment, patientGuid);
            if (resource != null) {
                resources.add(resource);
            }
        }
    }

    public static Resource transform(AppointmentType appointment, String patientGuid) throws TransformException {

        //although the schema defines an appointment type structure, I've not seen any come through, even
        //after manually adding appointments into Emis Web and testing the XML output
        return null;
        /*Appointment fhirAppointment = new Appointment();

        fhirAppointment.setId(appointment.getGUID());
        fhirAppointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));

        fhirAppointment.setStatus(getAppointmentStatus(appointment.getStatus()));

        if (!StringUtils.isBlank(appointment.getReason()))
        {
            CodeableConcept codeableConcept = new CodeableConcept();
            codeableConcept.setText(appointment.getReason());

            fhirAppointment.setReason(codeableConcept);
        }

        Date startTime = DateConverter.getDateAndTime(appointment.getAssignedDate(), appointment.getTime());
        fhirAppointment.setStart(startTime);

        Date endTime = DateConverter.addMinutesToTime(startTime, Integer.parseInt(appointment.getDuration()));
        fhirAppointment.setEnd(endTime);

        fhirAppointment.addSlot(EmisOpenHelper.createSlotReference(appointment.getSlotGUID()));

        Appointment.ParticipantRequired requiredStatus = Appointment.ParticipantRequired.REQUIRED;
        Appointment.ParticipationStatus participationstatus = Appointment.ParticipationStatus.ACCEPTED;


        Reference patientReference = EmisOpenHelper.createPatientReference(patientGuid);
        fhirAppointment.addParticipant(createParticipant(patientReference, requiredStatus, participationstatus));

        for (HolderStruct holder : appointment.getHolderList().getHolder()) {
            Reference practitionerReference = EmisOpenHelper.createPractitionerReference(holder.getGUID());
            fhirAppointment.addParticipant(createParticipant(practitionerReference, requiredStatus, participationstatus));
        }

        Reference locationReference = EmisOpenHelper.createLocationReference(appointment.getSiteGUID());
        fhirAppointment.addParticipant(createParticipant(locationReference, requiredStatus, participationstatus));

        return fhirAppointment;*/
    }

    public static List<Appointment> transform(String patientGuid, PatientAppointmentList patientAppointmentList) throws TransformException
    {
        List<Appointment> appointments = new ArrayList<Appointment>();

        for (AppointmentStruct appointment : patientAppointmentList.getAppointment())
            appointments.add(transformToAppointment(patientGuid, appointment));

        return appointments;
    }

    public static Appointment transformToAppointment(String patientGuid, AppointmentStruct appointmentStruct) throws TransformException
    {
        Appointment appointment = new Appointment();

        appointment.setId(appointmentStruct.getSlotGUID());
        appointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));

        appointment.setStatus(getAppointmentStatus(appointmentStruct.getStatus()));

        if (!StringUtils.isBlank(appointmentStruct.getReason()))
        {
            CodeableConcept codeableConcept = new CodeableConcept();
            codeableConcept.setText(appointmentStruct.getReason());

            appointment.setReason(codeableConcept);
        }

        Date startTime = DateConverter.getDateAndTime(appointmentStruct.getDate(), appointmentStruct.getStartTime());
        appointment.setStart(startTime);

        Date endTime = DateConverter.addMinutesToTime(startTime, Integer.parseInt(appointmentStruct.getDuration()));
        appointment.setEnd(endTime);

        appointment.addSlot(EmisOpenHelper.createSlotReference(appointmentStruct.getSlotGUID()));

        Appointment.ParticipantRequired requiredStatus = Appointment.ParticipantRequired.REQUIRED;
        Appointment.ParticipationStatus participationstatus = Appointment.ParticipationStatus.ACCEPTED;


        Reference patientReference = EmisOpenHelper.createPatientReference(patientGuid);
        appointment.addParticipant(createParticipant(patientReference, requiredStatus, participationstatus));

        for (HolderStruct holder : appointmentStruct.getHolderList().getHolder()) {
            Reference practitionerReference = EmisOpenHelper.createPractitionerReference(holder.getGUID());
            appointment.addParticipant(createParticipant(practitionerReference, requiredStatus, participationstatus));
        }

        Reference locationReference = EmisOpenHelper.createLocationReference(appointmentStruct.getSiteGUID());
        appointment.addParticipant(createParticipant(locationReference, requiredStatus, participationstatus));

        return appointment;
    }

    private static Appointment.AppointmentParticipantComponent createParticipant(Reference reference, Appointment.ParticipantRequired required, Appointment.ParticipationStatus status) throws TransformException
    {
        return new Appointment.AppointmentParticipantComponent()
                .setActor(reference)
                .setRequired(required)
                .setStatus(status);
    }

    private static Appointment.AppointmentStatus getAppointmentStatus(String status)
    {
        switch (status) {
            case "Slot Available":
            case "Booked": return Appointment.AppointmentStatus.BOOKED;

            case "Start Call":
            case "Quiet Send In":
            case "Send In":
            case "Arrived": return Appointment.AppointmentStatus.ARRIVED;

            case "Cannot Be Seen":
            case "DNA":
            case "Telephone - Not In":
            case "Walked Out":
            case "Visited - Not In": return Appointment.AppointmentStatus.NOSHOW;

            case "Left":
            case "Telephone - Complete":
            case "Visited": return Appointment.AppointmentStatus.FULFILLED;

            case "Unknown":
            default: return Appointment.AppointmentStatus.NULL;
        }
    }
}
