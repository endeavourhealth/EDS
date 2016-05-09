package org.endeavourhealth.core.messaging.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Exchange {
	// Properties are internal only
	public Properties properties;
	// Headers will remain outbound
	public Map<String, String> headers;
	public UUID exchangeId = null;
	public String body;

	public Exchange() {
		headers = new HashMap<>();
	}

	/**
	 * gets/sets
     */
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public UUID getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(UUID exchangeId) {
		this.exchangeId = exchangeId;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
