package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;

public class UIMedicationStatement extends UIResource<UIMedicationStatement> {
	private UIDate dateAuthorised;
	private UIPractitioner prescriber;
	private UIMedication medication;
	private String dosage;
	private String status;
	private UIQuantity authorisedQuantity;
	private Integer repeatsAllowed;
	private Integer repeatsIssued;
	private UIDate mostRecentIssue;
	private UICode authorisationType;

	public UIDate getDateAuthorised() {
		return dateAuthorised;
	}

	public UIMedicationStatement setDateAuthorised(UIDate dateAuthorised) {
		this.dateAuthorised = dateAuthorised;
		return this;
	}

	public UIPractitioner getPrescriber() {
		return prescriber;
	}

	public UIMedicationStatement setPrescriber(UIPractitioner prescriber) {
		this.prescriber = prescriber;
		return this;
	}

	public UIMedication getMedication() {
		return medication;
	}

	public UIMedicationStatement setMedication(UIMedication medication) {
		this.medication = medication;
		return this;
	}

	public String getDosage() {
		return dosage;
	}

	public UIMedicationStatement setDosage(String dosage) {
		this.dosage = dosage;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public UIMedicationStatement setStatus(String status) {
		this.status = status;
		return this;
	}

	public UIQuantity getAuthorisedQuantity() {
		return authorisedQuantity;
	}

	public UIMedicationStatement setAuthorisedQuantity(UIQuantity authorisedQuantity) {
		this.authorisedQuantity = authorisedQuantity;
		return this;
	}

	public UIDate getMostRecentIssue() {
		return mostRecentIssue;
	}

	public UIMedicationStatement setMostRecentIssue(UIDate mostRecentIssue) {
		this.mostRecentIssue = mostRecentIssue;
		return this;
	}

	public UICode getAuthorisationType() {
		return authorisationType;
	}

	public UIMedicationStatement setAuthorisationType(UICode authorisationType) {
		this.authorisationType = authorisationType;
		return this;
	}

	public Integer getRepeatsAllowed() {
		return repeatsAllowed;
	}

	public UIMedicationStatement setRepeatsAllowed(Integer repeatsAllowed) {
		this.repeatsAllowed = repeatsAllowed;
		return this;
	}

	public Integer getRepeatsIssued() {
		return repeatsIssued;
	}

	public UIMedicationStatement setRepeatsIssued(Integer repeatsIssued) {
		this.repeatsIssued = repeatsIssued;
		return this;
	}
}
