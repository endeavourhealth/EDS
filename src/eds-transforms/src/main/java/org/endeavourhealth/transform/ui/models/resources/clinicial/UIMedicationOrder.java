package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UIDate;

import java.util.List;

public class UIMedicationOrder extends UIResource<UIMedicationOrder> {
	private UIDate dateAuthorized;
	private UIDate dateEnded;
	private UIPractitioner prescriber;
	private UIMedication medication;
	private List<UIDosageInstruction> dosageInstructions;

	public UIDate getDateAuthorized() {
		return dateAuthorized;
	}

	public UIMedicationOrder setDateAuthorized(UIDate dateAuthorized) {
		this.dateAuthorized = dateAuthorized;
		return this;
	}

	public UIPractitioner getPrescriber() {
		return prescriber;
	}

	public UIMedicationOrder setPrescriber(UIPractitioner prescriber) {
		this.prescriber = prescriber;
		return this;
	}

	public UIMedication getMedication() {
		return medication;
	}

	public UIMedicationOrder setMedication(UIMedication medication) {
		this.medication = medication;
		return this;
	}

	public List<UIDosageInstruction> getDosageInstructions() {
		return dosageInstructions;
	}

	public UIMedicationOrder setDosageInstructions(List<UIDosageInstruction> dosageInstructions) {
		this.dosageInstructions = dosageInstructions;
		return this;
	}

	public UIDate getDateEnded() {
		return dateEnded;
	}

	public UIMedicationOrder setDateEnded(UIDate dateEnded) {
		this.dateEnded = dateEnded;
		return this;
	}
}
