package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;

public class UIDispenseRequest extends UIResource<UIDispenseRequest> {
	private int numberOfRepeatsAllowed;
	private UIQuantity expectedDuration;
	private UIQuantity quantity;

	public int getNumberOfRepeatsAllowed() {
		return numberOfRepeatsAllowed;
	}

	public UIDispenseRequest setNumberOfRepeatsAllowed(int numberOfRepeatsAllowed) {
		this.numberOfRepeatsAllowed = numberOfRepeatsAllowed;
		return this;
	}

	public UIQuantity getExpectedDuration() {
		return expectedDuration;
	}

	public UIDispenseRequest setExpectedDuration(UIQuantity expectedDuration) {
		this.expectedDuration = expectedDuration;
		return this;
	}

	public UIQuantity getQuantity() {
		return quantity;
	}

	public UIDispenseRequest setQuantity(UIQuantity quantity) {
		this.quantity = quantity;
		return this;
	}
}
