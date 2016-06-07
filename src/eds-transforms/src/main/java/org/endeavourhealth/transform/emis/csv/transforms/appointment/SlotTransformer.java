package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Slot;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SlotTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        Appointment_Slot parser = new Appointment_Slot(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createSlotAndAppointment(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createSlotAndAppointment(Appointment_Slot slotParser, FhirObjectStore objectStore) throws Exception {

        //ignore deleted slots
        if (slotParser.getDeleted()) {
            return;
        }

        String patientGuid = slotParser.getPatientGuid();

        Slot fhirSlot = new Slot();
        fhirSlot.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SLOT));
        objectStore.addToMap(patientGuid, fhirSlot);

        String slotGuid = slotParser.getSlotGuid();
        fhirSlot.setId(slotGuid);

        String sessionGuid = slotParser.getSessionGuid();
        fhirSlot.setSchedule(objectStore.createScheduleReference(sessionGuid, patientGuid));

        fhirSlot.setFreeBusyType(Slot.SlotStatus.BUSY);

        Date startDate = slotParser.getAppointmentStartDateTime();
        fhirSlot.setStart(startDate);

        //calculate expected end datetime from start, plus duration in mins
        long endMillis = startDate.getTime() + (slotParser.getPlannedDurationInMinutes() * 60 * 1000);
        Date endDate = new Date(endMillis);
        fhirSlot.setEnd(endDate);


        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));
        objectStore.addToMap(patientGuid, fhirAppointment);

        //use the same slot GUID as the appointment GUID, since it's a different resource type, it should be fine
        fhirAppointment.setId(slotGuid);
        fhirAppointment.setStart(startDate);
        fhirAppointment.setEnd(new Date(endMillis));
        fhirAppointment.addSlot(ReferenceHelper.createReference(ResourceType.Slot, slotGuid));

        Appointment.AppointmentParticipantComponent fhirParticipant = fhirAppointment.addParticipant();
        fhirParticipant.setActor(objectStore.createPatientReference(patientGuid));
        fhirParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        if (slotParser.getDidNotAttend()) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
        } else if (slotParser.getLeftDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.FULFILLED);
        } else if (slotParser.getSendInDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.ARRIVED);
        } else {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.BOOKED);
        }

    }
}