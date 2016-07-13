package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.audit.AuditEvent;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.configuration.PostMessageToLogConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostMessageToLog extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private PostMessageToLogConfig config;

	public PostMessageToLog(PostMessageToLogConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		AuditEvent auditEvent = getAuditEvent();

		try {
			AuditWriter.writeAuditEvent(exchange, auditEvent);
			LOG.debug("Message written to outbound log");
		} catch (Exception e) {
			LOG.error("Error writing exchange to audit", e);
			// throw new PipelineException(e.getMessage());
		}
	}

	private AuditEvent getAuditEvent() {
		if ("Receive".equals(config.getEventType()))
			return AuditEvent.RECEIVE;
		if ("Validate".equals(config.getEventType()))
			return AuditEvent.VALIDATE;
		if ("Send".equals(config.getEventType()))
			return AuditEvent.SEND;
		throw new IllegalArgumentException("Unknown audit event type");
	}
}
