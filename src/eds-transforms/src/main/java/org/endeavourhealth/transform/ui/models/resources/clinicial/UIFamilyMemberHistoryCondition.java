package org.endeavourhealth.transform.ui.models.resources.clinicial;

import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;

public class UIFamilyMemberHistoryCondition extends UIResource<UIFamilyMemberHistoryCondition> {
	private UICodeableConcept code;
	private UICodeableConcept outcome;

	public UICodeableConcept getCode() {
		return code;
	}

	public UIFamilyMemberHistoryCondition setCode(UICodeableConcept code) {
		this.code = code;
		return this;
	}

	public UICodeableConcept getOutcome() {
		return outcome;
	}

	public UIFamilyMemberHistoryCondition setOutcome(UICodeableConcept outcome) {
		this.outcome = outcome;
		return this;
	}
}
