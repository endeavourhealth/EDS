package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourHealth.subscriber.filer.PCRFiler;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourHealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.transform.common.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class PostToSubscriberWebService extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		TransformBatch transformBatch = getTransformBatch(exchange);
		UUID batchId = transformBatch.getBatchId();

		SubscriberBatch subscriberBatch = getSubscriberBatch(exchange);
		UUID exchangeId = exchange.getId();

		UUID queuedMessageId = subscriberBatch.getQueuedMessageId();
		String software = subscriberBatch.getSoftware();
		String softwareVersion = subscriberBatch.getSoftwareVersion();
		String endpoint = subscriberBatch.getEndpoint();

		try {
			QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
			String payload = queuedMessageDal.getById(queuedMessageId);

			sendToSubscriber(payload, exchangeId, batchId, software, softwareVersion, endpoint);

			queuedMessageDal.delete(queuedMessageId);

		} catch (Exception ex) {
			throw new PipelineException("Failed to send to " + software + " for exchange " + exchangeId + " and batch " + batchId + " and queued message " + queuedMessageId, ex);
		}
	}

	private void sendToSubscriber(String payload, UUID exchangeId, UUID batchId, String software, String softwareVersion, String endpoint) throws Exception {

		if (software.equals(MessageFormat.ENTERPRISE_CSV)) {
			EnterpriseFiler.file(batchId, payload, endpoint);

		} else if (software.equals(MessageFormat.PCR_CSV)) {
			PCRFiler.file(batchId, payload, endpoint);

		} else if (software.equals(MessageFormat.VITRUICARE_XML)) {
			sendHttpPost(payload, endpoint);

		} else {
			throw new PipelineException("Unsupported outbound software " + software + " for exchange " + exchangeId + " and batch " + batchId);
		}
	}

	private static void sendHttpPost(String payload, String configName) throws Exception {

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


		// add header
		//post.setHeader("User-Agent", USER_AGENT);

		/*List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
		urlParameters.add(new BasicNameValuePair("cn", ""));
		urlParameters.add(new BasicNameValuePair("locale", ""));
		urlParameters.add(new BasicNameValuePair("caller", ""));
		urlParameters.add(new BasicNameValuePair("num", "12345"));

		post.setEntity(new UrlEncodedFormEntity(urlParameters));*/

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
	}


	private static SubscriberBatch getSubscriberBatch(Exchange exchange) throws PipelineException {
		String subscriberBatchJson = exchange.getHeader(HeaderKeys.SubscriberBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(subscriberBatchJson, SubscriberBatch.class);
		} catch (IOException e) {
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}

	}

	private static TransformBatch getTransformBatch(Exchange exchange) throws PipelineException {
		String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
		} catch (IOException e) {
			throw new PipelineException("Error deserializing transformation batch JSON", e);
		}
	}

	/*@Override
	public void process(Exchange exchange) throws PipelineException {
		try {
			SubscriberBatch subscriberBatch = ObjectMapperPool.getInstance().readValue(exchange.getHeader(HeaderKeys.SubscriberBatch), SubscriberBatch.class);
			// Load transformed message from DB
			String outboundMessage = new QueuedMessageRepository().getById(subscriberBatch.getOutputMessageId()).getMessageBody();
			exchange.setBody(outboundMessage);
			// Set list of destinations
			exchange.setHeader(HeaderKeys.DestinationAddress, String.join(",", subscriberBatch.getEndpoints()));

			//TODO - send subscriber payload to endpoints

		} catch (IOException e) {
			LOG.error("Error deserializing subscriber batch JSON", e);
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}
		LOG.trace("Message subscribers identified");
	}*/
}
