package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

public class UIDosageInstruction extends UIResource<UIDosageInstruction> {
	private String instructions;
	private UICodeableConcept additionalInstructions;
	private String dose;
	private String rate;

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

	public String getDose() {
		return dose;
	}

	public UIDosageInstruction setDose(String dose) {
		this.dose = dose;
		return this;
	}

	public String getRate() {
		return rate;
	}

	public UIDosageInstruction setRate(String rate) {
		this.rate = rate;
		return this;
	}
}
