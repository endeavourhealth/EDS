package org.endeavourhealth.core.messaging.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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


	/**
	 * returns the transform batches from the exchange header key
	 */
	public static List<TransformBatch> getTransformBatches(Exchange exchange) throws PipelineException {
		String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);
		if (Strings.isNullOrEmpty(transformBatchJson)) {
			throw new PipelineException("No " + HeaderKeys.TransformBatch + " header in exchange " + exchange.getId());
		}

		//depending on whether new way or old way, we may have a single batch or an array of them - handle both
		try {
			//new way, we should have an array of TransformBatch objects
			TransformBatch[] arr = ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch[].class);
			return Lists.newArrayList(arr);

		} catch (IOException e) {

			//old way, we will just have a single transform batch Object
			try {
				TransformBatch batch = ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
				List<TransformBatch> ret = new ArrayList<>();
				ret.add(batch);
				return ret;

			} catch (IOException ex) {
				throw new PipelineException("Error deserializing TransformBatch(es) from JSON " + transformBatchJson + " in exchange " + exchange.getId(), ex);
			}
		}
	}
}
