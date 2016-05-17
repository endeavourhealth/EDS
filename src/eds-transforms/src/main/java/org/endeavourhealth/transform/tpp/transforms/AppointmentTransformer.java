package org.endeavourhealth.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.tpp.schema.AppointmentStatus;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.List;

public class AppointmentTransformer {

    public static void transform(org.endeavourhealth.transform.tpp.schema.Appointment tppAppointment, List<Resource> fhirResources) throws TransformException {

        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_APPOINTMENT));
        fhirResources.add(fhirAppointment);

        fhirAppointment.setId(tppAppointment.getAppointmentUID());
        //TODO - decide how to make the appointment UID globally unique

        fhirAppointment.setStatus(convertStatus(tppAppointment.getStatus()));

        XMLGregorianCalendar dateTime = tppAppointment.getDateTime();
        fhirAppointment.setStart(dateTime.toGregorianCalendar().getTime());

        BigDecimal duration = tppAppointment.getDuration();
        int durationInt = duration.toBigInteger().intValue();
        //TODO - source data supports appointments with float duration, but FHIR uses ints
        //TODO - happy to set duration or calculate end time instead?
        fhirAppointment.setMinutesDuration(durationInt);

        String userName = tppAppointment.getUserName();
        fhirAppointment.addParticipant(Fhir.createParticipant(ResourceType.Practitioner, userName));

        String site = tppAppointment.getSite();
        Location fhirLocation = Fhir.findLocationForName(fhirResources, site);
        fhirAppointment.addParticipant(Fhir.createParticipant(ResourceType.Location, fhirLocation.getId()));

        String clincType = tppAppointment.getClinicType();
        fhirAppointment.setType(Fhir.createCodeableConcept(clincType));

        String comments = tppAppointment.getComments();
        if (Strings.isNullOrEmpty(comments)) {
            fhirAppointment.setReason(Fhir.createCodeableConcept(comments));
        }

        //TODO - do we need to represent links from Appts and Events to Referrals?
        //String referralUid = tppAppointment.getLinkedReferralUID();

        //flags aren't important to third parties
        //List<String> flags = tppAppointment.getFlag();

        String patientId = Fhir.findPatientId(fhirResources);
        fhirAppointment.addParticipant(Fhir.createParticipant(ResourceType.Patient, patientId));

    }


    private static Appointment.AppointmentStatus convertStatus(AppointmentStatus tppStatus) throws TransformException {
        if (tppStatus == AppointmentStatus.BOOKED) {
            return Appointment.AppointmentStatus.BOOKED;

        } else if (tppStatus == AppointmentStatus.ARRIVED
                || tppStatus == AppointmentStatus.WAITING
                || tppStatus == AppointmentStatus.IN_PROGRESS) {
            return Appointment.AppointmentStatus.ARRIVED;

        } else if (tppStatus == AppointmentStatus.FINISHED) {
            return Appointment.AppointmentStatus.FULFILLED;

        } else if (tppStatus == AppointmentStatus.DID_NOT_ATTEND
                || tppStatus == AppointmentStatus.PATIENT_WALKED_OUT) {
            return Appointment.AppointmentStatus.NOSHOW;

        } else if (tppStatus == AppointmentStatus.CANCELLED_BY_PATIENT
                || tppStatus == AppointmentStatus.CANCELLED_BY_ORGANISATION) {
            return Appointment.AppointmentStatus.CANCELLED;

        } else {
            throw new TransformException("Unsupported appointment status " + tppStatus);
        }
    }
}
