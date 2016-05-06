package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToLog;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToQueue;
import org.endeavourhealth.core.messaging.pipeline.components.ValidateMessageType;
import org.endeavourhealth.core.messaging.pipeline.components.ValidateSender;

public class PipelineProcessor {
	private Pipeline pipeline;

	public PipelineProcessor(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public boolean execute(Exchange exchange) {
		try {
			for (Object processConfig : pipeline.getValidateSenderOrValidateMessageTypeOrPostMessageToLog()) {
				PipelineComponent component = getComponent(processConfig);
				component.process(exchange);
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private PipelineComponent getComponent(Object processConfig) {
		String xmlTagName = processConfig.getClass().getSimpleName();

		switch(xmlTagName) {
			case "ValidateSenderConfig":
				return new ValidateSender((ValidateSenderConfig) processConfig);
			case "ValidateMessageTypeConfig":
				return new ValidateMessageType((ValidateMessageTypeConfig) processConfig);
			case "PostMessageToLogConfig":
				return new PostMessageToLog((PostMessageToLogConfig) processConfig);
			case "PostMessageToQueueConfig":
				return new PostMessageToQueue((PostMessageToQueueConfig) processConfig);
			default:
				return null;
		}
	}
}
