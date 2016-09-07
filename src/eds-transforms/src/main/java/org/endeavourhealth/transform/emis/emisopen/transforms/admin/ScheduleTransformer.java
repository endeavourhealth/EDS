package org.endeavourhealth.transform.emis.emisopen.transforms.admin;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions.AppointmentSessionList;
import org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions.AppointmentSessionStruct;
import org.endeavourhealth.transform.emis.emisopen.schema.eomappointmentsessions.HolderStruct;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleTransformer
{
    public static List<Schedule> transform(AppointmentSessionList appointmentSessionList) throws TransformException
    {
        ArrayList<Schedule> result = new ArrayList<>();

        for (AppointmentSessionStruct appointmentSession : appointmentSessionList.getAppointmentSession())
            result.add(transformToSchedule(appointmentSession));

        return result;
    }

    private static Schedule transformToSchedule(AppointmentSessionStruct appointmentSession) throws TransformException
    {
        Schedule schedule = new Schedule();

        schedule.setId(appointmentSession.getGUID());

        schedule.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SCHEDULE));

        Period period = new Period()
            .setStart(DateConverter.getDateAndTime(appointmentSession.getDate(), appointmentSession.getStartTime()))
            .setEnd(DateConverter.getDateAndTime(appointmentSession.getDate(), appointmentSession.getEndTime()));

        schedule.setPlanningHorizon(period);

        HolderStruct firstPractitioner = appointmentSession.getHolderList().getHolder().stream().findFirst().orElse(null);

        if (firstPractitioner != null)
            schedule.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, firstPractitioner.getGUID()));

        schedule.setComment(appointmentSession.getName());

        List<HolderStruct> subsequentPractitioners = appointmentSession.getHolderList().getHolder().stream().skip(1).collect(Collectors.toList());

        for (HolderStruct subsequentPractitioner : subsequentPractitioners)
            schedule.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.SCHEDULE_ADDITIONAL_ACTOR)
                    .setValue(ReferenceHelper.createReference(ResourceType.Practitioner, subsequentPractitioner.getGUID())));

        if (appointmentSession.getSite() != null)
            schedule.addExtension(new Extension()
                    .setUrl(FhirExtensionUri.SCHEDULE_LOCATION)
                    .setValue(ReferenceHelper.createReference(ResourceType.Location, appointmentSession.getSite().getGUID())));

        return schedule;
    }
}
