package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Reference;

import java.util.Map;

public class DiagnosticOrderTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.DiagnosticOrder model = new org.endeavourhealth.core.xml.enterprise.DiagnosticOrder();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            DiagnosticOrder fhir = (DiagnosticOrder)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getSubject();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasEncounter()) {
                Reference encounterReference = (Reference)fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasOrderer()) {
                Reference practitionerReference = fhir.getOrderer();
                Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasEvent()) {
                DiagnosticOrder.DiagnosticOrderEventComponent event = fhir.getEvent().get(0);
                if (event.hasDateTimeElement()) {
                    DateTimeType dt = event.getDateTimeElement();
                    model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                    model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
                }
            }

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

