package org.endeavourhealth.transform.ui.models.resources.clinicial;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIFamilyMemberHistory extends UIClinicalResource<UIFamilyMemberHistory> {
	private List<UIFamilyMemberHistoryCondition> conditions;

	public List<UIFamilyMemberHistoryCondition> getConditions() {
		return conditions;
	}

	public UIFamilyMemberHistory setConditions(List<UIFamilyMemberHistoryCondition> conditions) {
		this.conditions = conditions;
		return this;
	}
}
