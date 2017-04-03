package org.endeavourhealth.transform.ui.models.resources.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.transform.ui.models.resources.UIResource;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UILocation extends UIResource<UILocation> {
	private String name;
	private String description;

	public String getName() {
		return name;
	}

	public UILocation setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public UILocation setDescription(String description) {
		this.description = description;
		return this;
	}
}
