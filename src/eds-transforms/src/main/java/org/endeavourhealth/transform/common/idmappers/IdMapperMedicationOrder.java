package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperMedicationOrder extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        MedicationOrder medicationOrder = (MedicationOrder)resource;

        super.mapResourceId(medicationOrder, serviceId, systemInstanceId);
        super.mapExtensions(medicationOrder, serviceId, systemInstanceId);

        if (medicationOrder.hasIdentifier()) {
            super.mapIdentifiers(medicationOrder.getIdentifier(), serviceId, systemInstanceId);
        }
        if (medicationOrder.hasPatient()) {
            super.mapReference(medicationOrder.getPatient(), serviceId, systemInstanceId);
        }
        if (medicationOrder.hasPrescriber()) {
            super.mapReference(medicationOrder.getPrescriber(), serviceId, systemInstanceId);
        }
        if (medicationOrder.hasEncounter()) {
            super.mapReference(medicationOrder.getEncounter(), serviceId, systemInstanceId);
        }
        if (medicationOrder.hasReason()) {
            try {
                super.mapReference(medicationOrder.getReasonReference(), serviceId, systemInstanceId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasMedication()) {
            try {
                super.mapReference(medicationOrder.getMedicationReference(), serviceId, systemInstanceId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasDosageInstruction()) {
            for (MedicationOrder.MedicationOrderDosageInstructionComponent dosage: medicationOrder.getDosageInstruction()) {
                if (dosage.hasSite()) {
                    try {
                        super.mapReference(dosage.getSiteReference(), serviceId, systemInstanceId);
                    } catch (Exception ex) {
                        //do nothing if not a reference
                    }
                }
            }
        }
        if (medicationOrder.hasDispenseRequest()) {
            try {
                super.mapReference(medicationOrder.getDispenseRequest().getMedicationReference(), serviceId, systemInstanceId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasPriorPrescription()) {
            super.mapReference(medicationOrder.getPriorPrescription(), serviceId, systemInstanceId);
        }
    }
}
