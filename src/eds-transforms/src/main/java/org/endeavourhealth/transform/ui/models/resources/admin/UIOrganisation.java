package org.endeavourhealth.transform.ui.models.resources.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UIOrganisation extends UIResource<UIOrganisation> {
	private String name;
	private String type;

	public String getName() {
		return name;
	}

	public UIOrganisation setName(String name) {
		this.name = name;
		return this;
	}

	public String getType() {
		return type;
	}

	public UIOrganisation setType(String type) {
		this.type = type;
		return this;
	}
}
