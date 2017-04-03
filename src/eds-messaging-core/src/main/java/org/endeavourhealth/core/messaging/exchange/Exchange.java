package org.endeavourhealth.core.messaging.exchange;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Exchange {
	private Exception exception;

	private Map<String, String> headers;
	private UUID exchangeId = null;
	private String body;
	private Date timestamp;

	public Exchange(UUID exchangeId, String body, Date timestamp) {
		this(exchangeId, body, new HashMap<>(), timestamp);
	}

	public Exchange(UUID exchangeId, String body, Map<String, String> headers, Date timestamp) {
		this.exchangeId = exchangeId;
		this.body = body;
		this.headers = headers;
		this.timestamp = timestamp;
	}

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

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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


	public void setHeader(String key, String value) {
		headers.put(key, value);
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public UUID getHeaderAsUuid(String key) {
		String s = getHeader(key);
		if (Strings.isNullOrEmpty(s)) {
			return null;
		} else {
			return UUID.fromString(s);
		}
	}

	/**
	 * utility fns to cut down duplicated code all over
	 */
	public String[] getHeaderAsStringArray(String headerKey) throws PipelineException {
		String json = getHeader(headerKey);
		try {
			return ObjectMapperPool.getInstance().readValue(json, String[].class);
		} catch (Exception e) {
			throw new PipelineException("Failed to read String[] from json " + json, e);
		}
	}
}
