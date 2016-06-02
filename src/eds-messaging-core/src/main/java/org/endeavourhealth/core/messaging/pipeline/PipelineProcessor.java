package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.components.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(PipelineProcessor.class);

	private Pipeline pipeline;

	public PipelineProcessor(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public boolean execute(Exchange exchange) {
		PipelineComponent component = null;
		try {
			for (Object processConfig : pipeline.getPipelineComponents()) {
				PopulateExchangeParameters(exchange, processConfig);
				component = getComponent(processConfig);
				component.process(exchange);
			}
			return true;
		}
		catch (PipelineException e) {
			// Gracefully handle pipeline error and send response
			if (component != null)
				LOG.error("Pipeline exception (" + component.toString() + ") : " + e.getMessage());
			else
				LOG.error("Pipeline exception (null) : " + e.getMessage());
			exchange.setException(e);
			return false;
		}
		catch (Exception e) {
			// Fatal error
			if (component != null)
				LOG.error("Fatal error (" + component.toString() + ") : " + e.getMessage());
			else
				LOG.error("Fatal error (null) : " + e.getMessage());

			exchange.setException(e);
			return false;
		}
	}

	private PipelineComponent getComponent(Object processConfig) throws PipelineException {
		String xmlTagName = processConfig.getClass().getSimpleName();

		switch(xmlTagName) {
			case "OpenEnvelopeConfig":
				return new OpenEnvelope((OpenEnvelopeConfig) processConfig);
			case "EnvelopMessageConfig":
				return new EnvelopMessage((EnvelopMessageConfig) processConfig);
			case "LoadSenderConfigurationConfig":
				return new LoadSenderConfiguration((LoadSenderConfigurationConfig) processConfig);
			case "ValidateSenderConfig":
				return new ValidateSender((ValidateSenderConfig) processConfig);
			case "ValidateMessageTypeConfig":
				return new ValidateMessageType((ValidateMessageTypeConfig) processConfig);
			case "PostMessageToLogConfig":
				return new PostMessageToLog((PostMessageToLogConfig) processConfig);
			case "PostMessageToExchangeConfig":
				return new PostMessageToExchange((PostMessageToExchangeConfig) processConfig);
			case "ReturnResponseAcknowledgementConfig":
				return new ReturnResponseAcknowledgement((ReturnResponseAcknowledgementConfig) processConfig);
			case "MessageTransformConfig":
				return new MessageTransform((MessageTransformConfig) processConfig);
			case "PostToEventLogConfig":
				return new PostToEventLog((PostToEventLogConfig) processConfig);
			case "RunDataDistributionProtocolsConfig":
				return new RunDataDistributionProtocols((RunDataDistributionProtocolsConfig) processConfig);
			case "PostToSubscriberWebServiceConfig":
				return new PostToSubscriberWebService((PostToSubscriberWebServiceConfig) processConfig);
			case "PostToRestConfig":
				return new PostToRest((PostToRestConfig) processConfig);
			default:
				throw new PipelineException("Unknown component : " + xmlTagName);
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
