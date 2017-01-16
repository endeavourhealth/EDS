package org.endeavourhealth.patientexplorer.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.admin.models.Item;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonReportParamOptions {

	private Boolean prompt = false;
	private String value = null;

	public JsonReportParamOptions() {
	}

	/**
	 * gets/sets
	 */
	public Boolean getPrompt() {
		return prompt;
	}

	public void setPrompt(Boolean prompt) {
		this.prompt = prompt;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
