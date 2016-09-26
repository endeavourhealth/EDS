package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class ObservationTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.Observation model = new org.endeavourhealth.core.xml.enterprise.Observation();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            Observation fhir = (Observation)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getSubject();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasPerformer()) {
                for (Reference reference: fhir.getPerformer()) {
                    ResourceType resourceType = ReferenceHelper.getResourceType(reference);
                    if (resourceType == ResourceType.Practitioner) {
                        UUID enterprisePractitionerUuid = findEnterpriseUuid(reference);
                        model.setPractitionerId(enterprisePractitionerUuid.toString());
                    }
                }
            }

            if (fhir.hasEffectiveDateTimeType()) {
                DateTimeType dt = fhir.getEffectiveDateTimeType();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getCode());
            model.setSnomedConceptId(snomedConceptId);

            if (fhir.hasValue()) {
                Quantity quantity = fhir.getValueQuantity();
                model.setValue(quantity.getValue());
                model.setUnits(quantity.getUnit());
            }
        }

        data.getObservation().add(model);
    }


}

