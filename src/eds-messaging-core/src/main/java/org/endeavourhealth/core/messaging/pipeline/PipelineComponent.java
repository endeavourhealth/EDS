package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
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

	/**
	 * sets the exchange ID, service ID and sender local ID in the logging, so all logging events
	 * have these values associated with them
     */
	private void setLoggingContext(Exchange exchange) {
		UUID exchangeId = exchange.getId();
		if (exchangeId != null) {
			MDC.put(LOGGING_KEY_EXCHANGE, exchangeId.toString());
		}
		String serviceId = exchange.getHeader(HeaderKeys.SenderServiceUuid);
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
