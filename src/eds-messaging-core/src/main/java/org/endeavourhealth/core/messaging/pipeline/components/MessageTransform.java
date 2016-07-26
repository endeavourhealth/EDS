package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MessageTransform extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransform.class);

	private MessageTransformConfig config;

	public MessageTransform(MessageTransformConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Transform to tech interface format
		//TODO - check source format and invoke relevant transform
		/*try {
			CsvProcessor processor = new CsvProcessor(exchange, serviceUuid, systemInstanceId);
			EmisCsvTransformer.transform(f.getSelectedFile().getAbsolutePath(), processor);

		} catch (Exception e) {
			LOG.error("Error", e);
		}*/

		LOG.debug("Message transformed");
	}
}
