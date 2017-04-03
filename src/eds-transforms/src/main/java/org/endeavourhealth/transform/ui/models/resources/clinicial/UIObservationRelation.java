package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;

public class UIObservationRelation extends UIResource<UIObservationRelation> {
	private String type;
	private UIObservation target;

	public String getType() {
		return type;
	}

	public UIObservationRelation setType(String type) {
		this.type = type;
		return this;
	}

	public UIObservation getTarget() {
		return target;
	}

	public UIObservationRelation setTarget(UIObservation target) {
		this.target = target;
		return this;
	}
}
