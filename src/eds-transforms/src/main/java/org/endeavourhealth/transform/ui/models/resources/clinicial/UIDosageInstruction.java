package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

public class UIDosageInstruction extends UIResource<UIDosageInstruction> {
	private String instructions;
	private UICodeableConcept additionalInstructions;

	public String getInstructions() {
		return instructions;
	}

	public UIDosageInstruction setInstructions(String instructions) {
		this.instructions = instructions;
		return this;
	}

	public UICodeableConcept getAdditionalInstructions() {
		return additionalInstructions;
	}

	public UIDosageInstruction setAdditionalInstructions(UICodeableConcept additionalInstructions) {
		this.additionalInstructions = additionalInstructions;
		return this;
	}
}
