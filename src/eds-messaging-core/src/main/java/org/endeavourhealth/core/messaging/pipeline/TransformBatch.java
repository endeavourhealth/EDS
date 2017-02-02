package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.*;

public class TransformBatch {
	private UUID batchId;
	private UUID protocolId;
	private Map<ResourceType, List<UUID>> resourceIds;
	private List<ServiceContract> subscribers;

	public TransformBatch() {
		resourceIds = new HashMap<>();
		subscribers = new ArrayList<>();
	}

	public UUID getBatchId() {
		return batchId;
	}

	public void setBatchId(UUID batchId) {
		this.batchId = batchId;
	}

	public UUID getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(UUID protocolId) {
		this.protocolId = protocolId;
	}

	public Map<ResourceType, List<UUID>> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Map<ResourceType, List<UUID>> resourceIds) {
		this.resourceIds = resourceIds;
	}

	public List<ServiceContract> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<ServiceContract> subscribers) {
		this.subscribers = subscribers;
	}
}
