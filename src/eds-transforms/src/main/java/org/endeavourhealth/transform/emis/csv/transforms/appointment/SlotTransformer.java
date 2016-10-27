package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Slot;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlotTransformer {

    public static void transform(String version,
                                 Map<Class, List<AbstractCsvParser>> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        for (AbstractCsvParser parser: parsers.get(Slot.class)) {

            while (parser.nextRecord()) {

                try {
                    createSlotAndAppointment((Slot)parser, csvProcessor, csvHelper);
                } catch (Exception ex) {
                    csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
                }
            }
        }
    }

    private static void createSlotAndAppointment(Slot parser,
                                                 CsvProcessor csvProcessor,
                                                 EmisCsvHelper csvHelper) throws Exception {

        String patientGuid = parser.getPatientGuid();

        //the slots CSV contains data on empty slots too; ignore them
        if (Strings.isNullOrEmpty(patientGuid)) {
            return;
        }

        //the EMIS data contains thousands of appointments that refer to patients we don't have, so I'm explicitly
        //handling this here, and ignoring any Slot record that is in this state
        UUID patientEdsId = IdHelper.getEdsResourceId(csvProcessor.getServiceId(), csvProcessor.getSystemId(), ResourceType.Patient, patientGuid);
        if (patientEdsId == null) {
            return;
        }

        org.hl7.fhir.instance.model.Slot fhirSlot = new org.hl7.fhir.instance.model.Slot();
        fhirSlot.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SLOT));

        String slotGuid = parser.getSlotGuid();
        EmisCsvHelper.setUniqueId(fhirSlot, patientGuid, slotGuid);

        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));

        //use the same slot GUID as the appointment GUID; since it's a different resource type, it should be fine
        EmisCsvHelper.setUniqueId(fhirAppointment, patientGuid, slotGuid);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted()) {
            csvProcessor.deletePatientResource(parser.getCurrentState(), patientGuid, fhirSlot, fhirAppointment);
            return;
        }

        String sessionGuid = parser.getSessionGuid();
        fhirSlot.setSchedule(csvHelper.createScheduleReference(sessionGuid));

        fhirSlot.setFreeBusyType(org.hl7.fhir.instance.model.Slot.SlotStatus.BUSY);

        Date startDate = parser.getAppointmentStartDateTime();

        //calculate expected end datetime from start, plus duration in mins
        long endMillis = startDate.getTime() + (parser.getPlannedDurationInMinutes() * 60 * 1000);
        Date endDate = new Date(endMillis);

        fhirSlot.setStart(startDate);
        fhirSlot.setEnd(endDate);

        fhirAppointment.setStart(startDate);
        fhirAppointment.setEnd(new Date(endMillis));

        Integer duration = parser.getActualDurationInMinutes();
        if (duration != null) {
            fhirAppointment.setMinutesDuration(duration.intValue());
        }

        Reference slotReference = ReferenceHelper.createReference(ResourceType.Slot, fhirSlot.getId());
        fhirAppointment.addSlot(slotReference);

        Appointment.AppointmentParticipantComponent fhirParticipant = fhirAppointment.addParticipant();
        fhirParticipant.setActor(csvHelper.createPatientReference(patientGuid));
        fhirParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        //the helper class has a list of our practitioners
        List<String> userGuids = csvHelper.findSessionPractionersToSave(sessionGuid, false);
        for (String userGuid: userGuids) {

            fhirParticipant = fhirAppointment.addParticipant();
            fhirParticipant.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, userGuid));
            fhirParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
        }

        if (parser.getDidNotAttend()) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
        } else if (parser.getLeftDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.FULFILLED);
        } else if (parser.getSendInDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.ARRIVED);
        } else {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.BOOKED);
        }

        Integer patientWaitMins = parser.getPatientWaitInMin();
        if (patientWaitMins != null) {
            Duration fhirDuration = QuantityHelper.createDuration(patientWaitMins, "minutes");
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_PATIENT_WAIT, fhirDuration));
        }

        Integer patientDelayMins = parser.getAppointmentDelayInMin();
        if (patientDelayMins != null) {
            Duration fhirDuration = QuantityHelper.createDuration(patientDelayMins, "minutes");
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_PATIENT_DELAY, fhirDuration));
        }

        Long dnaReasonCode = parser.getDnaReasonCodeId();
        if (dnaReasonCode != null) {

            CodeableConcept fhirCodeableConcept = csvHelper.findClinicalCode(dnaReasonCode);
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_DNA_REASON_CODE, fhirCodeableConcept));
        }

        Date sentInTime = parser.getSendInDateTime();
        if (sentInTime != null) {
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_SENT_IN, new DateTimeType(sentInTime)));
        }

        Date leftTime = parser.getLeftDateTime();
        if (leftTime != null) {
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_LEFT, new DateTimeType(leftTime)));
        }

        csvProcessor.savePatientResource(parser.getCurrentState(), patientGuid, fhirSlot, fhirAppointment);
    }
}