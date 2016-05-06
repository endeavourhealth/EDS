package org.endeavourhealth.core.messaging.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Exchange {
	// Properties are internal only
	public Properties properties;
	// Headers will remain outbound
	public Map<String, String> headers;
	public String body;

	public Exchange() {
		headers = new HashMap<>();
	}
}
