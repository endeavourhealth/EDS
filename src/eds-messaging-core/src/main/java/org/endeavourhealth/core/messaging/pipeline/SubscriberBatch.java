package org.endeavourhealth.core.messaging.pipeline;

import java.util.UUID;

public class SubscriberBatch {

	private UUID queuedMessageId = null;
	private String endpoint = null;
	private String software = null;
	private String softwareVersion = null;
	private UUID technicalInterfaceId = null;

	public UUID getQueuedMessageId() {
		return queuedMessageId;
	}

	public void setQueuedMessageId(UUID queuedMessageId) {
		this.queuedMessageId = queuedMessageId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getSoftware() {
		return software;
	}

	public void setSoftware(String software) {
		this.software = software;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public UUID getTechnicalInterfaceId() {
		return technicalInterfaceId;
	}

	public void setTechnicalInterfaceId(UUID technicalInterfaceId) {
		this.technicalInterfaceId = technicalInterfaceId;
	}
}
