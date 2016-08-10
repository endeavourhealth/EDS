package org.endeavourhealth.core.messaging.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Exchange {
	private Exception exception;

	private Map<String, String> headers;
	private UUID exchangeId = null;
	private String body;

	public Exchange(UUID exchangeId, String body) {
		this.exchangeId = exchangeId;
		this.headers = new HashMap<>();
		this.body = body;
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

	public void setHeader(String key, String value) {
		headers.put(key, value);
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
