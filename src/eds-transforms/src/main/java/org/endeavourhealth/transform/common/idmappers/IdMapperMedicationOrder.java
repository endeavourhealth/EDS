package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperMedicationOrder extends BaseIdMapper {

    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        MedicationOrder medicationOrder = (MedicationOrder)resource;

        if (medicationOrder.hasIdentifier()) {
            super.mapIdentifiers(medicationOrder.getIdentifier(), resource, serviceId, systemId);
        }
        if (medicationOrder.hasPatient()) {
            super.mapReference(medicationOrder.getPatient(), resource, serviceId, systemId);
        }
        if (medicationOrder.hasPrescriber()) {
            super.mapReference(medicationOrder.getPrescriber(), resource, serviceId, systemId);
        }
        if (medicationOrder.hasEncounter()) {
            super.mapReference(medicationOrder.getEncounter(), resource, serviceId, systemId);
        }
        if (medicationOrder.hasReason()) {
            try {
                super.mapReference(medicationOrder.getReasonReference(), resource, serviceId, systemId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasMedication()) {
            try {
                super.mapReference(medicationOrder.getMedicationReference(), resource, serviceId, systemId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasDosageInstruction()) {
            for (MedicationOrder.MedicationOrderDosageInstructionComponent dosage: medicationOrder.getDosageInstruction()) {
                if (dosage.hasSite()) {
                    try {
                        super.mapReference(dosage.getSiteReference(), resource, serviceId, systemId);
                    } catch (Exception ex) {
                        //do nothing if not a reference
                    }
                }
            }
        }
        if (medicationOrder.hasDispenseRequest()) {
            try {
                super.mapReference(medicationOrder.getDispenseRequest().getMedicationReference(), resource, serviceId, systemId);
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationOrder.hasPriorPrescription()) {
            super.mapReference(medicationOrder.getPriorPrescription(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(medicationOrder, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        MedicationOrder medicationOrder = (MedicationOrder)resource;
        if (medicationOrder.hasPatient()) {
            return ReferenceHelper.getReferenceId(medicationOrder.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
