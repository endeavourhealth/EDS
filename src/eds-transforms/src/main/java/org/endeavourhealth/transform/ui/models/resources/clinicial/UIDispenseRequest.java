package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.types.UIDate;

import java.util.List;

public class UIDispenseRequest extends UIResource<UIDispenseRequest> {
	private int numberOfRepeatsAllowed;

	public int getNumberOfRepeatsAllowed() {
		return numberOfRepeatsAllowed;
	}

	public UIDispenseRequest setNumberOfRepeatsAllowed(int numberOfRepeatsAllowed) {
		this.numberOfRepeatsAllowed = numberOfRepeatsAllowed;
		return this;
	}
}
