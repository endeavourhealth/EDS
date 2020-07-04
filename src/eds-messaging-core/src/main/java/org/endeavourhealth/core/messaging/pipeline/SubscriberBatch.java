package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriberBatch {

	private UUID queuedMessageId = null; //points to transformed data in DB
	private String endpoint = null; //subscriber config name
	private String software = null; //subscriber software type - remove once everything is in the config JSON
	private String softwareVersion = null; //subscriber software version - remove once everything is in the config JSON
	private UUID technicalInterfaceId = null; //can be removed once deployed and all queues clear

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
