package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.QuantityHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedicationOrder;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedicationStatement;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class UIMedicationOrderTransform extends UIClinicalTransform<MedicationOrder, UIMedicationOrder> {
		@Override
		public List<UIMedicationOrder> transform (List <MedicationOrder> resources, ReferencedResources referencedResources){
		return resources
				.stream()
				.map(t -> transform(t, referencedResources))
				.collect(Collectors.toList());
	}

	protected static Reference getMedicationStatementExtensionValue(DomainResource resource) {
		return ExtensionHelper.getExtensionValue(resource, FhirExtensionUri.MEDICATION_ORDER_AUTHORISATION, Reference.class);
	}

	private UIMedicationOrder transform(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
		return new UIMedicationOrder()
				.setId(medicationOrder.getId())
				.setMedicationStatement(getMedicationStatement(medicationOrder, referencedResources))
				.setDate(medicationOrder.getDateWritten())
				.setPrescriber(referencedResources.getUIPractitioner(medicationOrder.getPrescriber()))
				.setQuantity(getQuantity(medicationOrder.getDispenseRequest()))
				.setExpectedDuration(getExpectedDuration(medicationOrder.getDispenseRequest()));
	}

	private UIMedicationStatement getMedicationStatement(MedicationOrder medicationOrder, ReferencedResources referencedResources) {
		return referencedResources.getUIMedicationStatement(getMedicationStatementExtensionValue(medicationOrder));
	}

	private UIQuantity getQuantity(MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequest) {
			if(dispenseRequest != null && dispenseRequest.getQuantity() != null)
				return QuantityHelper.convert(dispenseRequest.getQuantity());
			return null;
	}

	private String getExpectedDuration(MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequest) {
			if (dispenseRequest != null && dispenseRequest.getExpectedSupplyDuration() != null) {
				Duration duration = dispenseRequest.getExpectedSupplyDuration();
				return duration.getValue().toString() + " " + duration.getUnit();
			}

			return null;
	}

	@Override
	public List<Reference> getReferences(List<MedicationOrder> resources) {
		return StreamExtension.concat(
				resources
						.stream()
						.filter(t -> t.hasPatient())
						.map(t -> t.getPatient()),
				resources
						.stream()
						.filter(t -> t.hasPrescriber())
						.map(t -> t.getPrescriber()),
				resources
					.stream()
						.map(t -> getMedicationStatementExtensionValue(t))
						.filter(t -> (t != null))
		).collect(Collectors.toList());
	}
}
