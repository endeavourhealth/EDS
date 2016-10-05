package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.hl7.fhir.instance.model.*;

import java.util.Map;

public class MedicationOrderTransformer extends AbstractTransformer {

    public void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception {

        org.endeavourhealth.core.xml.enterprise.MedicationOrder model = new org.endeavourhealth.core.xml.enterprise.MedicationOrder();

        if (!mapIdAndMode(resource, model)) {
            return;
        }

        //if it will be passed to Enterprise as an Insert or Update, then transform the remaining fields
        if (model.getSaveMode() == SaveMode.INSERT
                || model.getSaveMode() == SaveMode.UPDATE) {

            org.hl7.fhir.instance.model.MedicationOrder fhir = (org.hl7.fhir.instance.model.MedicationOrder)deserialiseResouce(resource);

            model.setOrganizationId(enterpriseOrganisationUuid);

            Reference patientReference = fhir.getPatient();
            Integer enterprisePatientUuid = findEnterpriseId(patientReference);
            model.setPatientId(enterprisePatientUuid);

            if (fhir.hasPrescriber()) {
                Reference practitionerReference = fhir.getPrescriber();
                Integer enterprisePractitionerUuid = findEnterpriseId(practitionerReference);
                model.setPractitionerId(enterprisePractitionerUuid);
            }

            if (fhir.hasEncounter()) {
                Reference encounterReference = fhir.getEncounter();
                Integer enterpriseEncounterUuid = findEnterpriseId(encounterReference);
                model.setEncounterId(enterpriseEncounterUuid);
            }

            if (fhir.hasDateWrittenElement()) {
                DateTimeType dt = fhir.getDateWrittenElement();
                model.setClinicalEffectiveDate(convertDate(dt.getValue()));
                model.setDatePrecisionId(convertDatePrecision(dt.getPrecision()));
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
                        Integer enterprisePractitionerUuid = findEnterpriseId(medicationStatementReference);
                        model.setMedicationStatementId(enterprisePractitionerUuid);
                    }
                }
            }
        }

        data.getMedicationOrder().add(model);
    }

}

