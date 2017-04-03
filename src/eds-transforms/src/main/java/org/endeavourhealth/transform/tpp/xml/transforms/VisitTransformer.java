package org.endeavourhealth.transform.tpp.xml.transforms;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ParticipantHelper;
import org.endeavourhealth.transform.common.FhirHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.tpp.xml.schema.Visit;
import org.endeavourhealth.transform.tpp.xml.schema.VisitStatus;
import org.hl7.fhir.instance.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

public class VisitTransformer {

    public static void transform(Visit tppVisit, List<Resource> fhirResources) throws TransformException {

        Appointment fhirAppointment = new Appointment();
        fhirAppointment.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_APPOINTMENT));
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

        fhirAppointment.addParticipant(ParticipantHelper.createParticipant(ResourceType.Practitioner, userName));

        String comments = tppVisit.getComments();
        if (Strings.isNullOrEmpty(comments)) {
            fhirAppointment.setReason(CodeableConceptHelper.createCodeableConcept(comments));
        }

        //TODO - do we need to represent links from Visits to Referrals?
        //String referralUid = tppVisit.getLinkedReferralUID();

        //we need to indicate that this was at the patient's home
        //TODO - how to properly indicate a visit was performed at the patient's home?
        CodeableConcept fhirCode = CodeableConceptHelper.createCodeableConcept("CsvPatient's Home");
        fhirAppointment.addParticipant(new Appointment.AppointmentParticipantComponent()
                .addType(fhirCode)
                .setRequired(Appointment.ParticipantRequired.REQUIRED)
                .setStatus(Appointment.ParticipationStatus.ACCEPTED));

        String patientId = FhirHelper.findResourceId(Patient.class, fhirResources);
        fhirAppointment.addParticipant(ParticipantHelper.createParticipant(ResourceType.Patient, patientId));
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
