package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransformBatch {
	private UUID batchId;
	private UUID protocolId;
	private List<UUID> resourceIds;
	private List<ServiceContract> subscribers;

	public TransformBatch() {
		resourceIds = new ArrayList<>();
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

	public List<UUID> getResourceIds() {
		return resourceIds;
	}

	public List<ServiceContract> getSubscribers() {
		return subscribers;
	}
}
