package org.endeavourhealth.messagingapi.endpoints;

import org.apache.http.HttpStatus;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.transform.common.AuditWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEndpoint.class);

	protected Response process(HttpHeaders headers, String body, Pipeline pipeline) {

		UUID exchangeId = UUID.randomUUID();

		Exchange exchange = new Exchange();
		exchange.setId(exchangeId);
		exchange.setBody(body);
		exchange.setTimestamp(new Date());
		exchange.setHeaders(new HashMap<>());

		for (String key : headers.getRequestHeaders().keySet()) {
			exchange.setHeader(key, headers.getHeaderString(key));
		}

		//commit what we've just received to the DB
		try {
			AuditWriter.writeExchange(exchange);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to write exchange to database", ex);
		}

		PipelineProcessor processor = new PipelineProcessor(pipeline);
		if (processor.execute(exchange)) {

			//changed to return the exchange ID to the poster, so they can log that against what they sent
			return Response
					.ok()
					.entity(exchangeId.toString())
					.build();

			/*return Response
					.ok()
					.entity(exchange.getBody())
					.build();*/
		} else {

			//possibly take out later, but for testing purposes, having visibility of these is useful
			if (exchange.getException() != null) {
				LOG.error("Error processing exchange " + exchange.getId(), exchange.getException());
			}

			return Response
					.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.entity(exchange.getException().getMessage())
					.build();
		}
	}
}
