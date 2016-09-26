package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.*;

import java.util.Map;
import java.util.UUID;

public class MedicationOrderTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 UUID enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.MedicationOrder model = new org.endeavourhealth.core.xml.enterprise.MedicationOrder();

        mapIdAndMode(resource, model);

        //if no ID was mapped, we don't want to pass to Enterprise
        if (model.getId() == null) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            org.hl7.fhir.instance.model.MedicationOrder fhir = (org.hl7.fhir.instance.model.MedicationOrder)deserialiseResouce(resource);

            model.setOrganisationId(enterpriseOrganisationUuid.toString());

            Reference patientReference = fhir.getPatient();
            UUID enterprisePatientUuid = findEnterpriseUuid(patientReference);
            model.setPatientId(enterprisePatientUuid.toString());

            if (fhir.hasPrescriber()) {
                Reference practitionerReference = fhir.getPrescriber();
                UUID enterprisePractitionerUuid = findEnterpriseUuid(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid.toString());
            }

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                UUID enterpriseEncounterUuid = findEnterpriseUuid(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid.toString());
            }

            if (fhir.hasDateWrittenElement()) {
                DateTimeType dt = fhir.getDateWrittenElement();
                model.setDate(convertDate(dt.getValue()));
                model.setDatePrecision(convertDatePrecision(dt.getPrecision()));
            }

            Long snomedConceptId = findSnomedConceptId(fhir.getMedicationCodeableConcept());
            model.setDmdId(snomedConceptId);

            if (fhir.hasDosageInstruction()) {
                if (fhir.getDosageInstruction().size() > 1) {
                    throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
                }

                MedicationOrder.MedicationOrderDosageInstructionComponent doseage = fhir.getDosageInstruction().get(0);
                model.setDose(doseage.getText());
            }

            if (fhir.hasDispenseRequest()) {
                MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequestComponent = fhir.getDispenseRequest();
                Quantity q = dispenseRequestComponent.getQuantity();
                model.setQuantityValue(q.getValue());
                model.setQuantityUnit(q.getUnit());

                Duration duration = dispenseRequestComponent.getExpectedSupplyDuration();
                if (!duration.getUnit().equalsIgnoreCase("days")) {
                    throw new TransformException("Unsupported medication order duration type [" + duration.getUnit() + "] for " + fhir.getId());
                }
                int days = duration.getValue().intValue();
                model.setDurationDays(new Integer(days));
            }

            if (fhir.hasExtension()) {
                for (Extension extension : fhir.getExtension()) {

                    if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_ORDER_ESTIMATED_COST)) {
                        DecimalType d = (DecimalType)extension.getValue();
                        model.setEstimatedCost(d.getValue());

                    } else if (extension.getUrl().equals(FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION)) {
                        Reference medicationStatementReference = (Reference)extension.getValue();
                        UUID enterprisePractitionerUuid = findEnterpriseUuid(medicationStatementReference);
                        model.setMedicationStatementId(enterprisePractitionerUuid.toString());
                    }
                }
            }
        }

        data.getMedicationOrder().add(model);
    }

}

