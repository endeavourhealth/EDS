package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.schema.EncounterParticipantType;
import org.hl7.fhir.instance.model.*;

import java.util.Map;

public class EncounterTransformer extends AbstractTransformer {

    public  void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Encounter model = new org.endeavourhealth.core.xml.enterprise.Encounter();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Encounter fhir = (Encounter)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

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
                        Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                        model.setPractitionerId(enterprisePractitionerUuid);
                    }
                }
            }

            if (fhir.hasAppointment()) {
                Reference appointmentReference = fhir.getAppointment();
                Integer enterpriseAppointmentUuid = findEnterpriseId(appointmentReference);
                model.setAppointmentId(enterpriseAppointmentUuid);
            }

            if (fhir.hasPeriod()) {
                Period period = fhir.getPeriod();
                DateTimeType dt = period.getStartElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
            }
        }

        data.getEncounter().add(model);
    }


}
