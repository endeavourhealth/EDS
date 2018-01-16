package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PGPDecryptConfig;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PGPDecrypt extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PGPDecrypt.class);

	private PGPDecryptConfig config;

	public PGPDecrypt(PGPDecryptConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
			LOG.debug("PGP Decryption complete");
	}
}
