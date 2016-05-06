package org.endeavourhealth.messaging.exchange;

import java.util.HashMap;
import java.util.Map;

public class Exchange {
	public Map<String, String> headers;
	public String body;

	public Exchange() {
		headers = new HashMap<>();
	}
}
