package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class AppointmentTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentTransformer.class);

    public void transform(ResourceByExchangeBatch resource,
                          OutputContainer data,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.outputModels.Appointment model = data.getAppointments();

        Integer enterpriseId = mapId(resource, model);
        if (enterpriseId == null) {
            return;

        } else if (resource.getIsDeleted()) {
            model.writeDelete(enterpriseId.intValue());

        } else {

            Appointment fhir = (Appointment)deserialiseResouce(resource);

            int id;
            int organisationId;
            int patientId;
            Integer practitionerId = null;
            Integer scheduleId = null;
            Date startDate = null;
            Integer plannedDuration = null;
            Integer actualDuration = null;
            int statusId;
            Integer patientWait = null;
            Integer patientDelay = null;
            Date sentIn = null;
            Date left = null;

            Integer enterprisePatientId = null;
            if (fhir.hasParticipant()) {
                for (Appointment.AppointmentParticipantComponent participantComponent: fhir.getParticipant()) {
                    Reference reference = participantComponent.getActor();
                    ReferenceComponents components = ReferenceHelper.getReferenceComponents(reference);

                    if (components.getResourceType() == ResourceType.Patient) {
                        enterprisePatientId = findEnterpriseId(data.getPatients(), reference);

                    } else if (components.getResourceType() == ResourceType.Practitioner) {
                        practitionerId = findEnterpriseId(data.getPractitioners(), reference);
                    }
                }
            }

            //the test pack has data that refers to deleted or missing patients, so if we get a null
            //patient ID here, then skip this resource
            if (enterprisePatientId == null) {
                LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                return;
            }

            id = enterpriseId.intValue();
            organisationId = enterpriseOrganisationUuid.intValue();
            patientId = enterprisePatientId.intValue();

            if (fhir.getSlot().size() > 1) {
                throw new TransformException("Cannot handle appointments linked to multiple slots " + fhir.getId());
            }
            Reference slotReference = fhir.getSlot().get(0);
            Slot fhirSlot = (Slot)findResource(slotReference, otherResources);
            if (fhirSlot != null) {

                Reference scheduleReference = fhirSlot.getSchedule();
                scheduleId = findEnterpriseId(data.getSchedules(), scheduleReference);
            }

            startDate = fhir.getStart();

            Date end = fhir.getEnd();
            if (startDate != null && end != null) {
                long millisDiff = end.getTime() - startDate.getTime();
                plannedDuration = new Integer((int)(millisDiff / (1000 * 60)));
            }

            if (fhir.hasMinutesDuration()) {
                int duration = fhir.getMinutesDuration();
                actualDuration = new Integer(duration);
            }

            Appointment.AppointmentStatus status = fhir.getStatus();
            statusId = status.ordinal();

            if (fhir.hasExtension()) {
                for (Extension extension: fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_PATIENT_WAIT)) {
                        Duration d = (Duration)extension.getValue();
                        if (!d.getUnit().equalsIgnoreCase("minutes")) {
                            throw new TransformException("Unsupported patient wait unit [" + d.getUnit() + "] in " + fhir.getId());
                        }
                        int i = d.getValue().intValue();
                        patientWait = new Integer(i);

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_PATIENT_DELAY)) {
                        Duration d = (Duration)extension.getValue();
                        if (!d.getUnit().equalsIgnoreCase("minutes")) {
                            throw new TransformException("Unsupported patient delay unit [" + d.getUnit() + "] in " + fhir.getId());
                        }
                        int i = d.getValue().intValue();
                        patientDelay = new Integer(i);

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_SENT_IN)) {
                        DateTimeType dt = (DateTimeType)extension.getValue();
                        sentIn = dt.getValue();

                    } else if (extension.getUrl().equals(FhirExtensionUri.APPOINTMENT_LEFT)) {
                        DateTimeType dt = (DateTimeType)extension.getValue();
                        left = dt.getValue();

                    }
                }
            }

            model.writeUpsert(id,
                organisationId,
                patientId,
                practitionerId,
                scheduleId,
                startDate,
                plannedDuration,
                actualDuration,
                statusId,
                patientWait,
                patientDelay,
                sentIn,
                left);
        }
    }

    /*public void transform(ResourceByExchangeBatch resource,
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
                        Integer enterprisePatientUuid = findEnterpriseId(new org.endeavourhealth.core.xml.enterprise.Patient(), reference);

                        //the test pack has data that refers to deleted or missing patients, so if we get a null
                        //patient ID here, then skip this resource
                        if (enterprisePatientUuid == null) {
                            LOG.warn("Skipping " + fhir.getResourceType() + " " + fhir.getId() + " as no Enterprise patient ID could be found for it");
                            return;
                        }

                        model.setPatientId(enterprisePatientUuid);

                    } else if (components.getResourceType() == ResourceType.Practitioner) {
                        Integer enterprisePractitionerUuid = findEnterpriseId(new Practitioner(), reference);
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
                Integer enterpriseScheduleUuid = findEnterpriseId(new Schedule(), scheduleReference);
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
    }*/


}

