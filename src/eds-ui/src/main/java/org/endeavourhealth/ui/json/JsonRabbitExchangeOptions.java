package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonRabbitExchangeOptions {
	private String type;
	private boolean auto_delete;
	private boolean durable;
	private boolean internal;
	private Map<String,String> arguments = new HashMap<>();

	public boolean isAuto_delete() {
		return auto_delete;
	}

	public void setAuto_delete(boolean auto_delete) {
		this.auto_delete = auto_delete;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}
}
