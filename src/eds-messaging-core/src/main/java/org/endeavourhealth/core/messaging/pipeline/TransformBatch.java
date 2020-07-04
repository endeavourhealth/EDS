package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformBatch {

	public enum TransformAction {
		DELTA,
		FULL_LOAD,
		FULL_DELETE;

/*		@JsonCreator
		public static TransformAction forValue(String value) {
			for (TransformAction a: values()) {
				if (a.name().equals(value)) {
					return a;
				}
			}
			throw new IllegalArgumentException("Unexpected action [" + value + "]");
		}*/
	}

	private UUID batchId; //used for audit purposes

	/*private UUID protocolId;
	private Map<ResourceType, List<UUID>> resourceIds;
	private List<ServiceContract> subscribers;*/

	//fields to replace the above
	private String subscriberConfigName;
	private TransformAction action;

	public TransformBatch() {
		//resourceIds = new HashMap<>(); don't set to non-null, so it won't get serialised to JSON and we can eventually remove
		//subscribers = new ArrayList<>(); don't set to non-null, so it won't get serialised to JSON and we can eventually remove
	}

	public UUID getBatchId() {
		return batchId;
	}

	public void setBatchId(UUID batchId) {
		this.batchId = batchId;
	}

	/*public UUID getProtocolId() {
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
	}*/

	public String getSubscriberConfigName() {
		return subscriberConfigName;
	}

	public void setSubscriberConfigName(String subscriberConfigName) {
		this.subscriberConfigName = subscriberConfigName;
	}

	public TransformAction getAction() {
		return action;
	}

	public void setAction(TransformAction action) {
		this.action = action;
	}
}
