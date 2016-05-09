package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public abstract class AbstractEndpoint {

	protected Response Process(HttpHeaders headers, String body, Pipeline pipeline) {

		Exchange exchange = new Exchange();

		for (String key : headers.getRequestHeaders().keySet())
			exchange.headers.put(key, headers.getHeaderString(key));
		exchange.body = body;

		PipelineProcessor processor = new PipelineProcessor(pipeline);
		processor.execute(exchange);

		return Response
				.ok()
				.entity(exchange.body)
				.build();

	}
}
