package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class EncounterTransformer extends AbstractTransformer {

    public  void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Encounter model = new org.endeavourhealth.core.xml.enterprise.Encounter();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasParticipant()) {

                for (Encounter.EncounterParticipantComponent participantComponent: fhir.getParticipant()) {

                    boolean primary = false;
                    for (CodeableConcept codeableConcept: participantComponent.getType()) {
                        for (Coding coding : codeableConcept.getCoding()) {
                            if (coding.getCode().equals(EncounterParticipantType.PRIMARY_PERFORMER.getCode())) {
                                primary = true;
                                break;
                            }
                        }
                    }

                    if (primary) {
                        Reference practitionerReference = participantComponent.getIndividual();
                        UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid.toString());
                    }
                }
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
