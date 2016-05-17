package org.endeavourhealth.transform.tpp.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.fhir.Fhir;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.endeavourhealth.transform.tpp.schema.Visit;
import org.endeavourhealth.transform.tpp.schema.VisitStatus;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

public class VisitTransformer {

    public static void transform(Visit tppVisit, List<Resource> fhirResources) throws TransformException {

        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_APPOINTMENT));
        fhirResources.add(fhirAppointment);

        fhirAppointment.setId(tppVisit.getVisitUID());
        //TODO - decide how to make the visit UID globally unique

        fhirAppointment.setStatus(convertStatus(tppVisit.getStatus()));

        XMLGregorianCalendar dateTime = tppVisit.getDateTime();
        fhirAppointment.setStart(dateTime.toGregorianCalendar().getTime());

        BigInteger duration = tppVisit.getDuration();
        int durationInt = duration.intValue();
        //TODO - happy to set duration or calculate end time instead?
        fhirAppointment.setMinutesDuration(durationInt);

        String userName = tppVisit.getUserName();
        fhirAppointment.addParticipant(Fhir.createParticipant(ResourceType.Practitioner, userName));

        String comments = tppVisit.getComments();
        if (Strings.isNullOrEmpty(comments)) {
            fhirAppointment.setReason(Fhir.createCodeableConcept(comments));
        }

        //TODO - do we need to represent links from Visits to Referrals?
        //String referralUid = tppVisit.getLinkedReferralUID();

        //we need to indicate that this was at the patient's home
        //TODO - how to properly indicate a visit was performed at the patient's home?
        CodeableConcept fhirCode = Fhir.createCodeableConcept("CsvPatient's Home");
        fhirAppointment.addParticipant(new Appointment.AppointmentParticipantComponent()
                .addType(fhirCode)
                .setRequired(Appointment.ParticipantRequired.REQUIRED)
                .setStatus(Appointment.ParticipationStatus.ACCEPTED));

        String patientId = Fhir.findPatientId(fhirResources);
        fhirAppointment.addParticipant(Fhir.createParticipant(ResourceType.Patient, patientId));
    }

    private static Appointment.AppointmentStatus convertStatus(VisitStatus tppStatus) throws TransformException {

        if (tppStatus == VisitStatus.BOOKED) {
            return Appointment.AppointmentStatus.BOOKED;

        } else if (tppStatus == VisitStatus.FINISHED) {
            return Appointment.AppointmentStatus.FULFILLED;

        } else if (tppStatus == VisitStatus.CANCELLED_BY_ORGANISATION
                || tppStatus == VisitStatus.CANCELLED_BY_PATIENT) {
            return Appointment.AppointmentStatus.CANCELLED;

        } else {
            throw new TransformException("Unsupported visit stats " + tppStatus);
        }
    }

}
