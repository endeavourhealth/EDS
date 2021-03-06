package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeSubscriberSendAudit;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PostToSubscriberWebService extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;
	private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		SubscriberBatch subscriberBatch = SubscriberBatch.getSubscriberBatch(exchange);
		UUID batchId = findBatchId(exchange);
		UUID queuedMessageId = subscriberBatch.getQueuedMessageId();

		//validate we've data to send to the subscriber
		if (queuedMessageId == null) {
			//we may legitimately not have a queued message ID if our message was flagged as the last
			//one in an exchange but the transform didn't generate any data to send to subscribers
			if (ExchangeHelper.isLastMessage(exchange)) {
				return;

			} else {
				//if we've got a null queued message ID but aren't the last message, then
				//something has gone weirdly wrong
				throw new PipelineException("Null queued message ID for exchange " + exchange);
			}
		}


		UUID exchangeId = exchange.getId();
		String subscriberConfigName = subscriberBatch.getEndpoint();

		try {
			QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
			String payload = queuedMessageDal.getById(queuedMessageId);

			//if the Queue Reader app is killed after deleting the queued message but before ACKing the RabbitMQ
			//message, then we end up in a state where the message is still in RabbitMQ but the queued message
			//has gone. So we should check our audit and see if we did successfully process this batch before,
			//in which case we can just skip this message
			if (payload == null
					&& wasQueuedMessageAlreadyApplied(exchangeId, batchId, subscriberConfigName, queuedMessageId)) {
				LOG.warn("Queued message " + queuedMessageId + " not found for batch " + batchId + " but audit shows this was sent to subscriber OK before");
				return;
			}

			//actually send the data to the subscriber (for local subscriber DBs this will actually apply it to the DB)
			sendToSubscriberNewWay(payload, exchangeId, batchId, queuedMessageId, subscriberConfigName);

			//audit the sucessful sending
			auditSending(exchangeId, batchId, subscriberConfigName, queuedMessageId, null);

			//tidy up queued message table
			queuedMessageDal.delete(queuedMessageId);

		} catch (Exception ex) {
			//audit the failure and throw the exception
			auditSending(exchangeId, batchId, subscriberConfigName, queuedMessageId, ex);
			throw new PipelineException("Failed to send to " + subscriberConfigName + " for exchange " + exchangeId + " and batch " + batchId + " and queued message " + queuedMessageId, ex);
		}
	}

	private UUID findBatchId(Exchange exchange) throws PipelineException {
		List<TransformBatch> transformBatches = TransformBatch.getTransformBatches(exchange);
		return MessageTransformOutbound.getBatchId(transformBatches);
	}


	private boolean wasQueuedMessageAlreadyApplied(UUID exchangeId, UUID batchId, String subscriberConfigName, UUID queuedMessageId) throws Exception {

		//get all audits of this exchange batch for this subscriber
		List<ExchangeSubscriberSendAudit> audits = auditRepository.getSubscriberSendAudits(exchangeId, batchId, subscriberConfigName);

		//see if one of those audits was for queued message ID we're missing and had no errors
		for (ExchangeSubscriberSendAudit audit: audits) {
			if (audit.getQueuedMessageId().equals(queuedMessageId)
					&& audit.getError() == null) {
				return true;
			}
		}

		return false;
	}

	private void auditSending(UUID exchangeId, UUID batchId, String subscriberConfigName, UUID queuedMessageId, Exception exception) {

		ExchangeSubscriberSendAudit audit = new ExchangeSubscriberSendAudit();
		audit.setExchangeId(exchangeId);
		audit.setExchangeBatchId(batchId);
		audit.setSubscriberConfigName(subscriberConfigName);
		audit.setInsertedAt(new Date());
		audit.setQueuedMessageId(queuedMessageId);

		if (exception != null) {
			TransformError errorWrapper = new TransformError();
			TransformErrorUtility.addTransformError(errorWrapper, exception, new HashMap<>());
			audit.setError(errorWrapper);
		}

		//catching and logging any exception here because we're potentially already inside catching an exception and it'll get confusing
		try {
			auditRepository.save(audit);
		} catch (Exception ex) {
			LOG.error("Error saving audit of transform for exchange " + exchangeId + " and batch " + batchId + " for " + subscriberConfigName, ex);
		}
	}

	private void sendToSubscriberNewWay(String payload, UUID exchangeId, UUID batchId, UUID queuedMessageId, String subscriberConfigName) throws Exception {

		SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

		if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
			EnterpriseFiler.file(batchId, queuedMessageId, payload, subscriberConfigName);

		} else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
			SubscriberFiler.file(batchId, queuedMessageId, payload, subscriberConfigName);

		} else {
			throw new PipelineException("Unsupported outbound software " + subscriberConfig.getSubscriberType() + " for exchange " + exchangeId + " and batch " + batchId);
		}
	}


}
