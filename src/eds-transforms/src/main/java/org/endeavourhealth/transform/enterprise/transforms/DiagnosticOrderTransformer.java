package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.EnterpriseData;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;
import java.util.UUID;

public class DiagnosticOrderTransformer extends AbstractTransformer {

    public static void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.transform.enterprise.schema.DiagnosticOrder model = new org.endeavourhealth.transform.enterprise.schema.DiagnosticOrder();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getMode() == INSERT
                || model.getMode() == UPDATE) {

            DiagnosticOrder fhir = (DiagnosticOrder)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getSubject();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            DiagnosticOrder.DiagnosticOrderEventComponent event = fhir.getEvent().get(0);
            DateTimeType dt = event.getDateTimeElement();
            model.setDate(convertDate(dt.getValue()));
            model.setDatePrecision(convertDatePrecision(dt.getPrecision()));

            if (fhir.getItem().size() > 1) {
                throw new TransformException("DiagnosticOrder with more than one item not supported");
            }
            DiagnosticOrder.DiagnosticOrderItemComponent item = fhir.getItem().get(0);
            Long snomedConceptId = findSnomedConceptId(item.getCode());
            model.setSnomedConceptId(snomedConceptId);
        }

        data.getDiagnosticOrder().add(model);
    }


}

