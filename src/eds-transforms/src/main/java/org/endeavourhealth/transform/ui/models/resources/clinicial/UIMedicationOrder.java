package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;

import java.util.Date;

public class UIMedicationOrder extends UIResource<UIMedicationOrder> {
	private UIMedicationStatement medicationStatement;
	private Date date;
	private UIPractitioner prescriber;
	private UIQuantity quantity;
	private String expectedDuration;

	public UIMedicationStatement getMedicationStatement() {
		return medicationStatement;
	}

	public UIMedicationOrder setMedicationStatement(UIMedicationStatement medicationStatement) {
		this.medicationStatement = medicationStatement;
		return this;
	}

	public Date getDate() {
		return date;
	}

	public UIMedicationOrder setDate(Date date) {
		this.date = date;
		return this;
	}

	public UIPractitioner getPrescriber() {
		return prescriber;
	}

	public UIMedicationOrder setPrescriber(UIPractitioner prescriber) {
		this.prescriber = prescriber;
		return this;
	}

	public UIQuantity getQuantity() {
		return quantity;
	}

	public UIMedicationOrder setQuantity(UIQuantity quantity) {
		this.quantity = quantity;
		return this;
	}

	public String getExpectedDuration() {
		return expectedDuration;
	}

	public UIMedicationOrder setExpectedDuration(String expectedDuration) {
		this.expectedDuration = expectedDuration;
		return this;
	}
}
