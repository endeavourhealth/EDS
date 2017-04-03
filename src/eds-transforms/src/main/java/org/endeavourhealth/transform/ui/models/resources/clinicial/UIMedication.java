package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

public class UIMedication extends UIResource<UIMedication> {
	private UICodeableConcept code;

	public UICodeableConcept getCode() {
		return code;
	}

	public UIMedication setCode(UICodeableConcept code) {
		this.code = code;
		return this;
	}
}
