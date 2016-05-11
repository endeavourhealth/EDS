package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.components.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private Pipeline pipeline;

	public PipelineProcessor(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public boolean execute(Exchange exchange) {
		try {
			for (Object processConfig : pipeline.getReadMessageEnvelopeOrValidateSenderOrValidateMessageType()) {
				PopulateExchangeParameters(exchange, processConfig);
				PipelineComponent component = getComponent(processConfig);
				component.process(exchange);
			}
			return true;
		}
		catch (PipelineException e) {
			// Gracefully handle pipeline error and send response
			LOG.error("Pipeline exception");
			// e.printStackTrace();
			return false;
		}
		catch (Exception e) {
			// Fatal error
			e.printStackTrace();
			return false;
		}
	}

	private PipelineComponent getComponent(Object processConfig) {
		String xmlTagName = processConfig.getClass().getSimpleName();

		switch(xmlTagName) {
			case "ReadMessageEnvelopeConfig":
				return new ReadMessageEnvelope((ReadMessageEnvelopeConfig) processConfig);
			case "ValidateSenderConfig":
				return new ValidateSender((ValidateSenderConfig) processConfig);
			case "ValidateMessageTypeConfig":
				return new ValidateMessageType((ValidateMessageTypeConfig) processConfig);
			case "PostMessageToLogConfig":
				return new PostMessageToLog((PostMessageToLogConfig) processConfig);
			case "PostMessageToQueueConfig":
				return new PostMessageToQueue((PostMessageToQueueConfig) processConfig);
			case "ReturnResponseAcknowledgementConfig":
				return new ReturnResponseAcknowledgement((ReturnResponseAcknowledgementConfig) processConfig);
			case "TransformMessageConfig":
				return new MessageTransform((MessageTransformConfig) processConfig);
			case "PostToEventLogConfig":
				return new PostToEventLog((PostToEventLogConfig) processConfig);
			case "RunDataDistributionProtocolsConfig":
				return new RunDataDistributionProtocols((RunDataDistributionProtocolsConfig) processConfig);
			case "PostToSubscriberWebServiceConfig":
				return new PostToSubscriberWebService((PostToSubscriberWebServiceConfig) processConfig);
			case "PostToSenderConfig":
				return new PostToSender((PostToSenderConfig) processConfig);
			default:
				return null;
		}
	}

	private void PopulateExchangeParameters(Exchange exchange, Object processConfig) {
		if (ComponentConfig.class.isAssignableFrom(processConfig.getClass())) {
			ComponentConfig config = ((ComponentConfig) processConfig);
			if (config.getExchangeProperties() != null && config.getExchangeProperties().getProperty() != null) {
				for (ExchangeProperty exchangeProperty : config.getExchangeProperties().getProperty()) {
					exchange.setProperty(exchangeProperty.getKey(), exchangeProperty.getValue());
				}
			}
		}
	}
}
