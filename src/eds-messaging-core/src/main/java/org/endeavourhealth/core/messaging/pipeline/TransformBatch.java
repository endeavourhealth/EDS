package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformBatch {

	public enum TransformAction {
		DELTA, //apply just the changes in the exchange_batch
		FULL_LOAD, //load all data for the patient (or all admin data)
		FULL_DELETE, //delete all data for the patient
		NONE; //don't make any changes
	}

	private UUID batchId;
	private String subscriberConfigName;
	private TransformAction action;

	public TransformBatch() {
	}

	public UUID getBatchId() {
		return batchId;
	}

	public void setBatchId(UUID batchId) {
		this.batchId = batchId;
	}

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
