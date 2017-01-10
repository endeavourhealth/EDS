package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class AppointmentTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Appointment model = new org.endeavourhealth.core.xml.enterprise.Appointment();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.UPSERT) {

            Appointment fhir = (Appointment)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            if (fhir.hasParticipant()) {
                for (Appointment.AppointmentParticipantComponent participantComponent: fhir.getParticipant()) {
                    Reference reference = participantComponent.getActor();
                    ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);

                    if (components.getResourceType() == ResourceType.Patient) {
                        Integer enterprisePatientUuid = findEnterpriseId(reference);

                        //the test pack has data that refers to deleted or missing patients, so if we get a null
                        //patient ID here, then skip this resource
                        if (enterprisePatientUuid == null) {
                            LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                            return;
                        }

                        model.setPatientId(enterprisePatientUuid);

                    } else if (components.getResourceType() == ResourceType.Practitioner) {
                        Integer enterprisePractitionerUuid = findEnterpriseId(reference);
                        model.setPractitionerId(enterprisePractitionerUuid);

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
                Integer enterpriseScheduleUuid = findEnterpriseId(scheduleReference);
                model.setScheduleId(enterpriseScheduleUuid);
            }

            Date start = fhir.getStart();
            model.setStartDate(convertDate(start));

            Date end = fhir.getEnd();
            if (start != null && end != null) {
                long millisDiff = end.getTime() - start.getTime();
                model.setPlannedDuration(new Integer((int)(millisDiff / (1000 * 60))));
            }

            if (fhir.hasMinutesDuration()) {
                int duration = fhir.getMinutesDuration();
                model.setActualDuration(new Integer(duration));
            }

            Appointment.AppointmentStatus status = fhir.getStatus();
            model.setAppointmentStatusId(status.ordinal());

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


}

