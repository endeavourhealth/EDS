package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.ForEachConfig;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ForEach extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ForEach.class);

	private ForEachConfig config;

	public ForEach(ForEachConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		try {
			if (config.getInList() != null) {
				processArray(exchange, config.getInList());
			} else if (config.getInCsv() != null) {
				processCsv(exchange, config.getInCsv());
			} else {
				throw new PipelineException("List not supplied for loop");
			}
		} catch (Exception e) {
			throw new PipelineException("Error executing loop", e);
		}
	}

	private void processArray(Exchange exchange, String headerKey) throws IOException {
		String listJson = exchange.getHeader(headerKey);
		Object[] objects = ObjectMapperPool.getInstance().readValue(listJson, Object[].class);
		executeLoop(exchange, objects);
	}

	private void processCsv(Exchange exchange, String headerKey) throws IOException {
		String csv = exchange.getHeader(headerKey);
		Object[] objects = csv.split(",", -1);
		executeLoop(exchange, objects);
	}

	private void executeLoop(Exchange exchange, Object[] objects) throws IOException {
		// Store original exchange
		String exchangeJson = ObjectMapperPool.getInstance().writeValueAsString(exchange);
		Pipeline pipeline = config.getPipeline();
		String headerKey = config.getHeader();

		LOG.debug("Looping " + objects.length + " objects");
		for(Object object : objects) {
			Exchange exchangeCopy = ObjectMapperPool.getInstance().readValue(exchangeJson, Exchange.class);
			exchangeCopy.setHeader(headerKey, ObjectMapperPool.getInstance().writeValueAsString(object));
			PipelineProcessor processor = new PipelineProcessor(pipeline);
			processor.execute(exchangeCopy);
		}
	}
}
