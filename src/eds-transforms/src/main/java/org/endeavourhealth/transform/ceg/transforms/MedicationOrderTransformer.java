package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.Map;

public class MedicationOrderTransformer extends AbstractTransformer {

    public static void transform(MedicationOrder fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        org.endeavourhealth.transform.ceg.models.Medication model = new org.endeavourhealth.transform.ceg.models.Medication();


        model.setPatientId(transformPatientId(fhir.getPatient()));
        model.setIssueDate(fhir.getDateWritten());

        CodeableConcept codeableConcept = fhir.getMedicationCodeableConcept();
        for (Coding coding: codeableConcept.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                String value = coding.getCode();
                model.setDmdCode(Long.parseLong(value));
                String term = coding.getDisplay();
                model.setMedicationTerm(term);
            }
        }

        if (fhir.hasDispenseRequest()) {
            MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequestComponent = fhir.getDispenseRequest();
            if (dispenseRequestComponent.hasQuantity()) {
                SimpleQuantity quantity = dispenseRequestComponent.getQuantity();
                double quantityValue = quantity.getValue().doubleValue();
                String quantityUnit = quantity.getUnit();

                model.setQuantity(new Double(quantityValue));
                model.setUnit(quantityUnit);
            }
        }

        if (fhir.hasPrescriber()) {
            Reference prescriberReference = fhir.getPrescriber();
            model.setStaffId(transformStaffId(prescriberReference));
        }

        DecimalType costType = (DecimalType)findExtension(fhir, FhirExtensionUri.MEDICATION_ORDER_ESTIMATED_COST);
        if (costType != null) {
            double cost = costType.getValue().doubleValue();
            model.setCost(new Double(cost));
        }

//TODO - finish
        /**
         private Integer medicationIssueId;
         */

        models.add(model);
    }
}
