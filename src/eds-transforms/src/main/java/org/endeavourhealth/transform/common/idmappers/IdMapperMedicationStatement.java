package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperMedicationStatement extends BaseIdMapper {

    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        MedicationStatement medicationStatement = (MedicationStatement)resource;

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

        return super.mapCommonResourceFields(medicationStatement, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        MedicationStatement medicationStatement = (MedicationStatement)resource;
        if (medicationStatement.hasPatient()) {
            return ReferenceHelper.getReferenceId(medicationStatement.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
