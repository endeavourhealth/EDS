package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.AppointmentStatus;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AppointmentTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Appointment model = new org.endeavourhealth.core.xml.enterprise.Appointment();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Appointment fhir = (Appointment)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            if (fhir.hasParticipant()) {
                for (Appointment.AppointmentParticipantComponent participantComponent: fhir.getParticipant()) {
                    Reference reference = participantComponent.getActor();
                    ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);

                    if (components.getResourceType() == ResourceType.Patient) {
                        UUID enterprisePatientUuid = findEnterpriseUuid(reference);
                        model.setPatientId(enterprisePatientUuid.toString());

                    } else if (components.getResourceType() == ResourceType.Practitioner) {
                        UUID enterprisePractitionerUuid = findEnterpriseUuid(reference);
                        model.setPractitionerId(enterprisePractitionerUuid.toString());

                    }
                }
            }

            if (fhir.getSlot().size() > 1) {
                throw new TransformException("Cannot handle appointments linked to multiple slots " + fhir.getId());
            }
            Reference slotReference = fhir.getSlot().get(0);
            Slot fhirSlot = (Slot)findResource(slotReference, otherResources);
            if (fhirSlot != null) {

                Reference scheduleReference = fhirSlot.getSchedule();
                UUID enterpriseScheduleUuid = findEnterpriseUuid(scheduleReference);
                model.setScheduleId(enterpriseScheduleUuid.toString());
            }

            Date start = fhir.getStart();
            model.setStart(convertDate(start));

            Date end = fhir.getEnd();
            model.setEnd(convertDate(end));

            if (fhir.hasMinutesDuration()) {
                int duration = fhir.getMinutesDuration();
                model.setActualDuration(new Integer(duration));
            }

            if (fhir.hasStatus()) {
                Appointment.AppointmentStatus status = fhir.getStatus();
                model.setStatus(convertStatus(status));
            }

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_PATIENT_WAIT)) {
                        Duration d = (Duration)extension.getValue();
                        if (!d.getUnit().equalsIgnoreCase("minutes")) {
                            throw new TransformException("Unsupported patient wait unit [" + d.getUnit() + "] in " + fhir.getId());
                        }
                        int i = d.getValue().intValue();
                        model.setPatientWait(new Integer(i));

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_PATIENT_DELAY)) {
                        Duration d = (Duration)extension.getValue();
                        if (!d.getUnit().equalsIgnoreCase("minutes")) {
                            throw new TransformException("Unsupported patient delay unit [" + d.getUnit() + "] in " + fhir.getId());
                        }
                        int i = d.getValue().intValue();
                        model.setPatientDelay(new Integer(i));

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_SENT_IN)) {
                        DateTimeType dt = (DateTimeType)extension.getValue();
                        model.setSentIn(convertDate(dt.getValue()));

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_LEFT)) {
                        DateTimeType dt = (DateTimeType)extension.getValue();
                        model.setLeft(convertDate(dt.getValue()));

                    }
                }
            }
        }

        data.getAppointment().add(model);
    }

    private static AppointmentStatus convertStatus(Appointment.AppointmentStatus status) throws Exception {
        switch (status) {
            case PROPOSED:
                return AppointmentStatus.PROPOSED;
            case PENDING:
                return AppointmentStatus.PENDING;
            case BOOKED:
                return AppointmentStatus.BOOKED;
            case ARRIVED:
                return AppointmentStatus.ARRIVED;
            case FULFILLED:
                return AppointmentStatus.FULFILLED;
            case CANCELLED:
                return AppointmentStatus.CANCELLED;
            case NOSHOW:
                return AppointmentStatus.NOSHOW;
            default:
                throw new TransformException("Unsupported appointment status " + status);
        }
    }


}

