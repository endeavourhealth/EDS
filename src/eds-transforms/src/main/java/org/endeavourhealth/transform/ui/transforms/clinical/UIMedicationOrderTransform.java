package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.QuantityHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIDispenseRequest;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIDosageInstruction;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedication;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedicationOrder;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;
import java.util.stream.Collectors;

public class UIMedicationOrderTransform extends UIClinicalTransform<MedicationOrder, UIMedicationOrder> {
    @Override
    public List<UIMedicationOrder> transform(List<MedicationOrder> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private UIMedicationOrder transform(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
        return new UIMedicationOrder()
            .setId(medicationOrder.getId())
            .setDateAuthorized(DateHelper.convert(medicationOrder.getDateWrittenElement()))
						.setDateEnded(DateHelper.convert(medicationOrder.getDateEndedElement()))
            .setPrescriber(getPrescriber(medicationOrder, referencedResources))
            .setMedication(getMedication(medicationOrder, referencedResources))
						.setDosageInstructions(getDosageInstructions(medicationOrder, referencedResources))
						.setDispenseRequest(getDispenseRequest(medicationOrder));
    }

    @Override
    public List<Reference> getReferences(List<MedicationOrder> resources) {
        try {
            return StreamExtension.concat(
                resources
                    .stream()
                    .filter(MedicationOrder::hasPrescriber)
                    .map(MedicationOrder::getPrescriber),
                resources
                    .stream()
                    .filter(t -> hasMedicationReference(t))
                    .map(t -> getMedicationReference(t))
            )
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static boolean hasMedicationReference(MedicationOrder medicationOrder) {
    	try {
    		return medicationOrder.hasMedicationReference();
			}
			catch (Exception e) {
    		return false;
			}
		}

		private static Reference getMedicationReference(MedicationOrder medicationOrder) {
    	try {
    		return medicationOrder.getMedicationReference();
			}
			catch (Exception e) {
    		return null;
			}
		}

    private static UIPractitioner getPrescriber(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
        return referencedResources.getUIPractitioner(medicationOrder.getPrescriber());
    }

    private static UIMedication getMedication(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
        try {
            if (medicationOrder.hasMedicationCodeableConcept())
                return new UIMedication()
										.setCode(CodeHelper.convert(medicationOrder.getMedicationCodeableConcept()));
            if (medicationOrder.hasMedicationReference())
                return referencedResources.getUIMedication(medicationOrder.getMedicationReference());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<UIDosageInstruction> getDosageInstructions(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
    	return medicationOrder.getDosageInstruction().stream()
					.map(t -> getDosageInstruction(t, referencedResources))
					.collect(Collectors.toList());
		}

		private static UIDosageInstruction getDosageInstruction(MedicationOrder.MedicationOrderDosageInstructionComponent dosageInstruction, ReferencedResources referencedResources) {
    	return new UIDosageInstruction()
					.setInstructions(dosageInstruction.getText())
					.setAdditionalInstructions(CodeHelper.convert(dosageInstruction.getAdditionalInstructions()))
					.setDose(getDose(dosageInstruction));
		}

		private static UIDispenseRequest getDispenseRequest(MedicationOrder medicationOrder) {
    	if (!medicationOrder.hasDispenseRequest())
    		return null;

    	return new UIDispenseRequest()
					.setNumberOfRepeatsAllowed(medicationOrder.getDispenseRequest().getNumberOfRepeatsAllowed())
					.setExpectedDuration(QuantityHelper.convert(medicationOrder.getDispenseRequest().getExpectedSupplyDuration()))
					.setQuantity(QuantityHelper.convert(medicationOrder.getDispenseRequest().getQuantity()));
		}

		private static String getDose(MedicationOrder.MedicationOrderDosageInstructionComponent dosageInstruction) {
    	if (!dosageInstruction.hasDose())
    		return "";
    	try {
				if (dosageInstruction.hasDoseSimpleQuantity())
					return dosageInstruction.getDoseSimpleQuantity().getValue().toString();
			}
			catch (Exception e) {}

			try {
    		if (dosageInstruction.hasDoseRange())
    			return dosageInstruction.getDoseRange().toString();
			} catch (Exception e){}

			return "";
		}
}
