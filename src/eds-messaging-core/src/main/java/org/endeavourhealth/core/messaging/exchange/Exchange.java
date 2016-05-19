package org.endeavourhealth.core.messaging.exchange;

import org.endeavourhealth.core.messaging.EDSMethod;

import java.util.*;

public class Exchange {
	// Properties are internal only. Use to pass data between components
	private Properties properties;
	private Exception exception;

	private EDSMethod method;
	private String requester;

	// Headers will remain outbound
	private Map<String, String> headers;
	private UUID exchangeId = null;
	private String body;

	public Exchange() {
		this.properties = new Properties();
		this.headers = new HashMap<>();
	}

	public Exchange(String body) {
		this.properties = new Properties();
		this.headers = new HashMap<>();
		this.body = body;
	}

	public Exchange(Map<String, String> headers, String body) {
		this.properties = new Properties();
		this.headers = headers;
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

	public void setMethod(EDSMethod method) {
		this.method = method;
	}

	public EDSMethod getMethod() {
		return this.method;
	}

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public String getRequester() {
		return requester;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
