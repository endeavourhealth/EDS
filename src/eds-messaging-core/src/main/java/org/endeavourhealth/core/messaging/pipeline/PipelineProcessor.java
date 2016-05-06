package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.configuration.PostMessageToQueueConfig;
import org.endeavourhealth.core.configuration.ValidateMessageTypeConfig;
import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToInboundQueue;
import org.endeavourhealth.core.messaging.pipeline.components.ValidateMessageType;
import org.endeavourhealth.core.messaging.pipeline.components.ValidateSender;

public class PipelineProcessor {
	private Pipeline pipeline;

	public PipelineProcessor(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public boolean execute(Exchange exchange) {
		try {
			for (Object processConfig : pipeline.getValidateSenderOrValidateMessageTypeOrPostMessageToQueue()) {
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
			case "ValidateSender":
				return new ValidateSender((ValidateSenderConfig) processConfig);
			case "ValidateMessageType":
				return new ValidateMessageType((ValidateMessageTypeConfig) processConfig);
			case "PostMessageToQueue":
				return new PostMessageToInboundQueue((PostMessageToQueueConfig) processConfig);
			default:
				return null;
		}
	}
}
