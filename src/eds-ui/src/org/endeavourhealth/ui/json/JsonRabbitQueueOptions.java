package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonRabbitQueueOptions {
	private boolean auto_delete;
	private boolean durable;

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
}
