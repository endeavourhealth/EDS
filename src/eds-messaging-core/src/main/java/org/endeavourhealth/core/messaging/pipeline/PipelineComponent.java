package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class PipelineComponent {
	public abstract void process(Exchange exchange) throws PipelineException;

	protected IParser getParser(String contentType) {
		if (contentType == null || contentType.isEmpty())
			return new JsonParser();

		if ("text/xml".equals(contentType) || "application/xml".equals(contentType))
			return new XmlParser();

		return new JsonParser();
	}
}
