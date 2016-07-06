package org.endeavourhealth.core.messaging.pipeline.components;

import org.apache.http.HttpStatus;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

public class PostToSubscriberWebService implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		String subscriberList = exchange.getHeader(HeaderKeys.Subscribers);
		if (subscriberList == null || subscriberList.isEmpty()) {
			LOG.info("No subscriber addresses provided");
			return;
		}

		String[] subscriberEndpoints = subscriberList.split(",", -1);

		for (String subscriberEndpoint : subscriberEndpoints) {
			if (postToSubscriber(exchange, subscriberEndpoint))
				LOG.debug("Message posted to subscriber [" + subscriberEndpoint + "]");
			else
				LOG.debug("Failed to post message to subscriber [" + subscriberEndpoint + "]");
		}
	}

	private boolean postToSubscriber(Exchange exchange, String subscriberEndpoint) {
		Client client = ClientBuilder.newClient( new ClientConfig().register( LoggingFilter.class ) );
		WebTarget webTarget = client.target(subscriberEndpoint);

		String format = exchange.getHeader(HeaderKeys.ContentType);
		Invocation.Builder invocationBuilder =  webTarget.request(format);

		for(String key : exchange.getHeaders().keySet())
			invocationBuilder.header(key, exchange.getHeader(key));

		Entity entity = Entity.entity(exchange.getBody(), format);

		Response response = invocationBuilder.post(entity);

		exchange.setBody(response.readEntity(String.class));

		return (response.getStatus() == HttpStatus.SC_OK);
	}
}
