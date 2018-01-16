package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class LogErrors extends PipelineComponent {

	@Override
	public void process(Exchange exchange) {
		// Figure out the transformer
		// Call it
		// Replace message body in echange
	}
}
