package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;

public class UIMedicationStatement extends UIResource<UIMedicationStatement> {
	private UIDate dateAuthorized;
	private UIPractitioner prescriber;
	private UIMedication medication;
	private String dosage;
	private String status;
	private UIQuantity authorizedQuantity;
	private Integer repeatsAllowed;
	private Integer repeatsIssued;
	private UIDate mostRecentIssue;
	private UICode authorizationType;

	public UIDate getDateAuthorized() {
		return dateAuthorized;
	}

	public UIMedicationStatement setDateAuthorized(UIDate dateAuthorized) {
		this.dateAuthorized = dateAuthorized;
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

	public UIQuantity getAuthorizedQuantity() {
		return authorizedQuantity;
	}

	public UIMedicationStatement setAuthorizedQuantity(UIQuantity authorizedQuantity) {
		this.authorizedQuantity = authorizedQuantity;
		return this;
	}

	public UIDate getMostRecentIssue() {
		return mostRecentIssue;
	}

	public UIMedicationStatement setMostRecentIssue(UIDate mostRecentIssue) {
		this.mostRecentIssue = mostRecentIssue;
		return this;
	}

	public UICode getAuthorizationType() {
		return authorizationType;
	}

	public UIMedicationStatement setAuthorizationType(UICode authorizationType) {
		this.authorizationType = authorizationType;
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
