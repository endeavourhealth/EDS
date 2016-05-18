package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.messaging.EDSMethod;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public abstract class AbstractEndpoint {

	protected Response Process(HttpHeaders headers, String body, Pipeline pipeline) {

		Exchange exchange = new Exchange(body);

		for (String key : headers.getRequestHeaders().keySet())
			exchange.setHeader(key, headers.getHeaderString(key));

		if (headers.getHeaderString("Content-Type") != null)
			exchange.setProperty(PropertyKeys.Format, headers.getHeaderString("Content-Type"));

		PipelineProcessor processor = new PipelineProcessor(pipeline);
		processor.execute(exchange);

		return Response
				.ok()
				.entity(exchange.getBody())
				.build();

	}
}
