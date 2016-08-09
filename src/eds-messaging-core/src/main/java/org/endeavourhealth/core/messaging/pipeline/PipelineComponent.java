package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.slf4j.MDC;

import java.util.UUID;

public abstract class PipelineComponent {

	private static final String LOGGING_KEY_EXCHANGE = "ExchangeId";
	private static final String LOGGING_KEY_SERVICE = "ServiceId";
	private static final String LOGGING_KEY_LOCAL_IDENTIFIER = "SenderLocalIdentifier";

	public final void baseProcess(Exchange exchange) throws PipelineException {
		try {
			setLoggingContext(exchange);
			process(exchange);
		} finally {
			clearLoggingContext();
		}
	}

	protected abstract void process(Exchange exchange) throws PipelineException;

	protected IParser getParser(String contentType) {
		if (contentType == null || contentType.isEmpty())
			return new JsonParser();

		if ("text/xml".equals(contentType) || "application/xml".equals(contentType))
			return new XmlParser();

		return new JsonParser();
	}

	/**
	 * sets the exchange ID, service ID and sender local ID in the logging, so all logging events
	 * have these values associated with them
     */
	private void setLoggingContext(Exchange exchange) {
		UUID exchangeId = exchange.getExchangeId();
		if (exchangeId != null) {
			MDC.put(LOGGING_KEY_EXCHANGE, exchangeId.toString());
		}
		String serviceId = exchange.getHeader(HeaderKeys.SenderUuid);
		if (serviceId != null) {
			MDC.put(LOGGING_KEY_SERVICE, serviceId);
		}
		String senderLocalIdentifier = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
		if (senderLocalIdentifier != null) {
			MDC.put(LOGGING_KEY_LOCAL_IDENTIFIER, senderLocalIdentifier);
		}
	}

	private void clearLoggingContext() {
		MDC.clear();
	}
}
