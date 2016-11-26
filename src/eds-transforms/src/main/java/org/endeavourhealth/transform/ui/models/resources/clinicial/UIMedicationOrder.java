package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.models.types.UIDate;

public class UIMedicationOrder extends UIResource<UIMedicationOrder> {
	private UIDate dateAuthorized;
	private UIPractitioner prescriber;
	private UIMedication medication;

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
}
