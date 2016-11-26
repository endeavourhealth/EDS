package org.endeavourhealth.transform.ui.transforms.clinical;

import org.apache.commons.lang3.time.DateUtils;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.ceg.models.Medication;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedication;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedicationOrder;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.Reference;

import java.util.ArrayList;
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
            .setPrescriber(getPrescriber(medicationOrder, referencedResources))
            .setMedication(getMedication(medicationOrder, referencedResources));
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
        List<UICodeableConcept> medication = new ArrayList<>();

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
}
