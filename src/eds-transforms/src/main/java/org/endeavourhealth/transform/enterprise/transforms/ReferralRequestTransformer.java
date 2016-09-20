package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ReferralRequest;

import java.util.Map;
import java.util.UUID;

public class ReferralRequestTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.ReferralRequest model = new org.endeavourhealth.transform.enterprise.schema.ReferralRequest();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            ReferralRequest fhir = (ReferralRequest)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasRequester()) {
                Reference practitionerReference = fhir.getRequester();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            DateTimeType dt = fhir.getDateElement();
            model.setDate(convertDate(dt.getValue()));
            model.setDatePrecision(convertDatePrecision(dt.getPrecision()));

            Long snomedConceptId = findSnomedConceptId(fhir.getType());
            model.setSnomedConceptId(snomedConceptId);

            //TODO - referral fields!!!
            /**
            protected String recipientOrganisationId;
            protected String urgency;
             protected String serviceRequested;
             protected String type;
             */
        }

        data.getReferralRequest().add(model);
    }


}

