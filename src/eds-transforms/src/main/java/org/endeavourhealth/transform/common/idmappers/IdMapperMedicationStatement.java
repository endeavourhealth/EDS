package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperMedicationStatement extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        MedicationStatement medicationStatement = (MedicationStatement)resource;

        super.mapResourceId(medicationStatement, serviceId, systemId);
        super.mapExtensions(medicationStatement, serviceId, systemId);

        if (medicationStatement.hasIdentifier()) {
            super.mapIdentifiers(medicationStatement.getIdentifier(), resource, serviceId, systemId);
        }
        if (medicationStatement.hasPatient()) {
            super.mapReference(medicationStatement.getPatient(), resource, serviceId, systemId);
        }
        if (medicationStatement.hasInformationSource()) {
            super.mapReference(medicationStatement.getInformationSource(), resource, serviceId, systemId);
        }
        if (medicationStatement.hasReasonForUse()) {
            try {
                if (medicationStatement.hasReasonForUseReference()) {
                    super.mapReference(medicationStatement.getReasonForUseReference(), resource, serviceId, systemId);
                }
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationStatement.hasMedication()) {
            try {
                if (medicationStatement.hasMedicationReference()) {
                    super.mapReference(medicationStatement.getMedicationReference(), resource, serviceId, systemId);
                }
            } catch (Exception ex) {
                //do nothing if not a reference
            }
        }
        if (medicationStatement.hasDosage()) {
            for (MedicationStatement.MedicationStatementDosageComponent dosage: medicationStatement.getDosage()) {
                try {
                    if (dosage.hasSiteReference()) {
                        super.mapReference(dosage.getSiteReference(), resource, serviceId, systemId);
                    }
                } catch (Exception ex) {
                    //do nothing if not a reference
                }
            }
        }
    }
}
