package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Slot;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

public class SlotTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Slot parser = new Slot(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createSlotAndAppointment(parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createSlotAndAppointment(Slot slotParser,
                                                 CsvProcessor csvProcessor,
                                                 EmisCsvHelper csvHelper) throws Exception {

        String patientGuid = slotParser.getPatientGuid();

        //the slots CSV contains data on empty slots too; ignore them
        if (Strings.isNullOrEmpty(patientGuid)) {
            return;
        }

        org.hl7.fhir.instance.model.Slot fhirSlot = new org.hl7.fhir.instance.model.Slot();
        fhirSlot.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SLOT));

        String slotGuid = slotParser.getSlotGuid();
        EmisCsvHelper.setUniqueId(fhirSlot, patientGuid, slotGuid);

        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));

        //use the same slot GUID as the appointment GUID; since it's a different resource type, it should be fine
        EmisCsvHelper.setUniqueId(fhirAppointment, patientGuid, slotGuid);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (slotParser.getDeleted()) {
            csvProcessor.deletePatientResource(patientGuid, fhirSlot, fhirAppointment);
            return;
        }

        String sessionGuid = slotParser.getSessionGuid();
        fhirSlot.setSchedule(csvHelper.createScheduleReference(sessionGuid));

        fhirSlot.setFreeBusyType(org.hl7.fhir.instance.model.Slot.SlotStatus.BUSY);

        Date startDate = slotParser.getAppointmentStartDateTime();

        //calculate expected end datetime from start, plus duration in mins
        long endMillis = startDate.getTime() + (slotParser.getPlannedDurationInMinutes() * 60 * 1000);
        Date endDate = new Date(endMillis);

        fhirSlot.setStart(startDate);
        fhirSlot.setEnd(endDate);

        fhirAppointment.setStart(startDate);
        fhirAppointment.setEnd(new Date(endMillis));

        Integer duration = slotParser.getActualDurationInMinutes();
        if (duration != null) {
            fhirAppointment.setMinutesDuration(duration.intValue());
        }

        Reference slotReference = ReferenceHelper.createReference(ResourceType.Slot, fhirSlot.getId());
        fhirAppointment.addSlot(slotReference);

        Appointment.AppointmentParticipantComponent fhirParticipant = fhirAppointment.addParticipant();
        fhirParticipant.setActor(csvHelper.createPatientReference(patientGuid));
        fhirParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        //the helper class has a list of our practitioners
        List<String> userGuids = csvHelper.findSessionPractionersToSave(sessionGuid);
        for (String userGuid: userGuids) {

            fhirParticipant = fhirAppointment.addParticipant();
            fhirParticipant.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, userGuid));
            fhirParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
        }

        if (slotParser.getDidNotAttend()) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
        } else if (slotParser.getLeftDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.FULFILLED);
        } else if (slotParser.getSendInDateTime() != null) {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.ARRIVED);
        } else {
            fhirAppointment.setStatus(Appointment.AppointmentStatus.BOOKED);
        }

        Integer patientWaitMins = slotParser.getPatientWaitInMin();
        if (patientWaitMins != null) {
            Duration fhirDuration = QuantityHelper.createDuration(patientWaitMins, "minutes");
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_PATIENT_WAIT, fhirDuration));
        }

        Integer patientDelayMins = slotParser.getAppointmentDelayInMin();
        if (patientDelayMins != null) {
            Duration fhirDuration = QuantityHelper.createDuration(patientDelayMins, "minutes");
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_PATIENT_DELAY, fhirDuration));
        }

        Long dnaReasonCode = slotParser.getDnaReasonCodeId();
        if (dnaReasonCode != null) {
            CodeableConcept fhirCodeableConcept = CodeableConceptHelper.createCodeableConcept(FhirValueSetUri.VALUE_SET_EMIS_DNA_REASON_CODE, "", dnaReasonCode.toString());
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_DNA_REASON_CODE, fhirCodeableConcept));
        }

        Date sentInTime = slotParser.getSendInDateTime();
        if (sentInTime != null) {
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_SENT_IN, new DateTimeType(sentInTime)));
        }

        Date leftTime = slotParser.getLeftDateTime();
        if (leftTime != null) {
            fhirAppointment.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.APPOINTMENT_LEFT, new DateTimeType(leftTime)));
        }

        csvProcessor.savePatientResource(patientGuid, fhirSlot, fhirAppointment);
    }
}