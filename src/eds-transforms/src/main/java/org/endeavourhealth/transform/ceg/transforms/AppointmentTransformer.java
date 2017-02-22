package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Appointment;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AppointmentTransformer extends AbstractTransformer {


    public static void transform(org.hl7.fhir.instance.model.Appointment fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Appointment model = new Appointment();

        for (org.hl7.fhir.instance.model.Appointment.AppointmentParticipantComponent paricipant : fhir.getParticipant()) {
            Reference ref = paricipant.getActor();
            ReferenceComponents comps = ReferenceHelper.getReferenceComponents(ref);
            ResourceType resourceType = comps.getResourceType();

            if (resourceType == ResourceType.Patient) {
                model.setPatientId(transformPatientId(ref));
            }
        }

        Date start = fhir.getStart();
        model.setAppointmentDate(start);

        Date end = fhir.getEnd();
        model.setAppointmentEndTime(end);

        Duration dur = (Duration)findExtension(fhir, FhirExtensionUri.APPOINTMENT_PATIENT_DELAY);
        if (dur != null) {
            String timeUnit = dur.getUnit();
            if (timeUnit.equalsIgnoreCase("min")
                    || timeUnit.equalsIgnoreCase("mins")
                    || timeUnit.equalsIgnoreCase("minute")
                    || timeUnit.equalsIgnoreCase("minutes")) {

                BigDecimal bc = dur.getValue();
                int mins = bc.toBigInteger().intValue();

                long ms = start.getTime() - (mins * 60 * 1000);
                model.setArrivalTime(new Date(ms));
            }
        }

        Type t = findExtension(fhir, FhirExtensionUri.APPOINTMENT_SENT_IN);
        model.setSeenTime(transformDate(t));

        String status = fhir.getStatus().getDisplay();
        model.setCurrentStatus(status);

        if (fhir.hasSlot()) {
            Reference slotReference = fhir.getSlot().get(0);
            Slot fhirSlot = (Slot)findResource(slotReference, hsAllResources);
            if (fhirSlot != null) {
                Reference scheduleReference = fhirSlot.getSchedule();
                Schedule fhirSchedule = (Schedule)findResource(scheduleReference, hsAllResources);
                if (fhirSchedule != null) {

                    if (fhirSchedule.hasActor()) {
                        Reference actorReference = fhirSchedule.getActor();
                        Practitioner practitioner = (Practitioner)findResource(actorReference, hsAllResources);
                        if (practitioner != null) {

                            String name = practitioner.getName().getText();
                            model.setSessionHolder(name);
                        }
                    }

                    if (fhirSchedule.hasType()) {
                        CodeableConcept codeableConcept = fhirSchedule.getType().get(0);
                        String type = codeableConcept.getText();
                        model.setSessionType(type);
                    }

                    Reference locationReference = (Reference)findExtension(fhirSchedule, FhirExtensionUri.SCHEDULE_LOCATION);
                    if (locationReference != null) {

                        Location fhirLocation = (Location)findResource(locationReference, hsAllResources);
                        if (fhirLocation != null) {
                            String location = fhirLocation.getName();
                            model.setSessionLocation(location);
                        }

                    }
                }
            }
        }

        models.add(model);
    }
}
