package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeSubscriberSendAudit;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

		List<TransformBatch> transformBatches = MessageTransformOutbound.getTransformBatches(exchange);
		UUID batchId = MessageTransformOutbound.getBatchId(transformBatches);

		SubscriberBatch subscriberBatch = getSubscriberBatch(exchange);
		UUID exchangeId = exchange.getId();

		UUID queuedMessageId = subscriberBatch.getQueuedMessageId();
		//String software = subscriberBatch.getSoftware();
		//String softwareVersion = subscriberBatch.getSoftwareVersion(); //never used, so don't bother
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

			sendToSubscriberNewWay(payload, exchangeId, batchId, queuedMessageId, subscriberConfigName);

			auditSending(exchangeId, batchId, subscriberConfigName, queuedMessageId, null);

			queuedMessageDal.delete(queuedMessageId);

		} catch (Exception ex) {
			auditSending(exchangeId, batchId, subscriberConfigName, queuedMessageId, ex);
			throw new PipelineException("Failed to send to " + subscriberConfigName + " for exchange " + exchangeId + " and batch " + batchId + " and queued message " + queuedMessageId, ex);
		}
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

	/*private void sendToSubscriberOldWay(String payload, UUID exchangeId, UUID batchId, UUID queuedMessageId, String software, String endpoint) throws Exception {

		if (software.equals(MessageFormat.ENTERPRISE_CSV)) {
			EnterpriseFiler.file(batchId, queuedMessageId, payload, endpoint);

		} else if (software.equals(MessageFormat.PCR_CSV)) {
			PCRFiler.file(batchId, payload, endpoint);

		} else if (software.equals(MessageFormat.SUBSCRIBER_CSV)) {
			SubscriberFiler.file(batchId, queuedMessageId, payload, endpoint);

		} else {
			throw new PipelineException("Unsupported outbound software " + software + " for exchange " + exchangeId + " and batch " + batchId);
		}
	}*/

	/*private static void sendHttpPost(String payload, String configName) throws Exception {

		//String url = "http://127.0.0.1:8002/notify";
		//String url = "http://localhost:8002";
		//String url = "http://posttestserver.com/post.php";

		JsonNode config = ConfigManager.getConfigurationAsJson(configName, "vitruCare");
		String url = config.get("url").asText();

		if (url == null || url.length() <= "http://".length()) {
			LOG.trace("No/invalid url : [" + url + "]");
			return;
		}

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		HttpEntity entity = new ByteArrayEntity(payload.getBytes("UTF-8"));
		post.setEntity(entity);

		LOG.trace("Sending 'POST' request to URL : " + url);
		LOG.trace("Post parameters : " + post.getEntity());

		HttpResponse response = client.execute(post);
		LOG.trace("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		LOG.trace(result.toString());

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			throw new IOException("Failed to post to " + url);
		}
	}*/


	private static SubscriberBatch getSubscriberBatch(Exchange exchange) throws PipelineException {
		String subscriberBatchJson = exchange.getHeader(HeaderKeys.SubscriberBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(subscriberBatchJson, SubscriberBatch.class);
		} catch (IOException e) {
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}

	}


}
