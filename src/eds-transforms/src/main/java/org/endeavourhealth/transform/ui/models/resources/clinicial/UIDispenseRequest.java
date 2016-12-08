package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UIDate;

import java.util.List;

public class UIDispenseRequest extends UIResource<UIDispenseRequest> {
	private int numberOfRepeatsAllowed;
	private String expectedDuration;
	private String quantity;

	public int getNumberOfRepeatsAllowed() {
		return numberOfRepeatsAllowed;
	}

	public UIDispenseRequest setNumberOfRepeatsAllowed(int numberOfRepeatsAllowed) {
		this.numberOfRepeatsAllowed = numberOfRepeatsAllowed;
		return this;
	}

	public String getExpectedDuration() {
		return expectedDuration;
	}

	public UIDispenseRequest setExpectedDuration(String expectedDuration) {
		this.expectedDuration = expectedDuration;
		return this;
	}

	public String getQuantity() {
		return quantity;
	}

	public UIDispenseRequest setQuantity(String quantity) {
		this.quantity = quantity;
		return this;
	}
}
