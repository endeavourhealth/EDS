package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class EncounterTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.Encounter model = new org.endeavourhealth.transform.enterprise.schema.Encounter();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasParticipant()) {
                if (fhir.getParticipant().size() > 1) {
                    throw new TransformException("Cannot transform Encounters with more than one participant " + fhir.getId());
                }
                Encounter.EncounterParticipantComponent participantComponent = fhir.getParticipant().get(0);
                Reference practitionerReference = participantComponent.getIndividual();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasAppointment()) {
                Reference appointmentReference = fhir.getAppointment();
                UUID enterpriseAppointmentUuid = findEnterpriseUuid(appointmentReference);
                model.setAppointmentId(enterpriseAppointmentUuid.toString());
            }

            if (fhir.hasPeriod()) {
                Period period = fhir.getPeriod();
                DateTimeType dt = period.getStartElement();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            if (fhir.hasReason()) {
                if (fhir.getReason().size() > 1) {
                    throw new TransformException("Cannot transform encounters with more than one reason " + fhir.getId());
                }
                CodeableConcept cc = fhir.getReason().get(0);
                Long snomedConceptId = findSnomedConceptId(cc);
                model.setReasonSnomedConceptId(snomedConceptId);
            }
        }

        data.getEncounter().add(model);
    }


}
