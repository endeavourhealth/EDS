package org.endeavourhealth.core.messaging.pipeline.components;

import org.apache.http.HttpStatus;
import org.endeavourhealth.core.configuration.PostToRestConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

public class PostToRest implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToRest.class);

	private PostToRestConfig config;

	public PostToRest(PostToRestConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String responseAddress = (String)exchange.getProperty(PropertyKeys.DestinationAddress);
		if (responseAddress == null || responseAddress.isEmpty()) {
			LOG.info("Response address not provided");
			return;
		}

		Client client = ClientBuilder.newClient( new ClientConfig().register( LoggingFilter.class ) );
		WebTarget webTarget = client.target(responseAddress);

		Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_XML);

		for(String key : exchange.getHeaders().keySet())
			invocationBuilder.header(key, exchange.getHeader(key));

		String format = (String)exchange.getProperty(PropertyKeys.Format);

		Entity entity = Entity.entity(exchange.getBody(), format);

		Response response = invocationBuilder.post(entity);

		exchange.setBody(response.readEntity(String.class));

		if (response.getStatus() == HttpStatus.SC_OK)
			LOG.debug("Message posted");
		else {
			LOG.error("Error posting response to sender");
			throw new PipelineException(exchange.getBody());
		}
	}
}
