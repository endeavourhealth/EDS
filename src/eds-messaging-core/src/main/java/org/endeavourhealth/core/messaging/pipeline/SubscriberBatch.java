package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;

import java.io.IOException;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriberBatch {

	private UUID queuedMessageId = null; //points to transformed data in DB
	private String endpoint = null; //subscriber config name

	public SubscriberBatch() {
	}

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


	public static SubscriberBatch getSubscriberBatch(Exchange exchange) throws PipelineException {

		String subscriberBatchJson = exchange.getHeader(HeaderKeys.SubscriberBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(subscriberBatchJson, SubscriberBatch.class);
		} catch (IOException e) {
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}
	}

}
