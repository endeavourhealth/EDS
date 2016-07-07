package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperMedicationStatement extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        MedicationStatement medicationStatement = (MedicationStatement)resource;

        super.mapResourceId(medicationStatement, serviceId, systemInstanceId);
        super.mapExtensions(medicationStatement, serviceId, systemInstanceId);

        if (medicationStatement.hasIdentifier()) {
            super.mapIdentifiers(medicationStatement.getIdentifier(), serviceId, systemInstanceId);
        }
        if (medicationStatement.hasPatient()) {
            super.mapReference(medicationStatement.getPatient(), serviceId, systemInstanceId);
        }
        if (medicationStatement.hasInformationSource()) {
            super.mapReference(medicationStatement.getInformationSource(), serviceId, systemInstanceId);
        }
        if (medicationStatement.hasReasonForUse()) {
            try {
                if (medicationStatement.hasReasonForUseReference()) {
                    super.mapReference(medicationStatement.getReasonForUseReference(), serviceId, systemInstanceId);
                }
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationStatement.hasMedication()) {
            try {
                if (medicationStatement.hasMedicationReference()) {
                    super.mapReference(medicationStatement.getMedicationReference(), serviceId, systemInstanceId);
                }
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationStatement.hasDosage()) {
            for (MedicationStatement.MedicationStatementDosageComponent dosage: medicationStatement.getDosage()) {
                try {
                    if (dosage.hasSiteReference()) {
                        super.mapReference(dosage.getSiteReference(), serviceId, systemInstanceId);
                    }
                } catch (Exception ex) {
                    //do nothing if not a reference
                }
            }
        }
    }
}
