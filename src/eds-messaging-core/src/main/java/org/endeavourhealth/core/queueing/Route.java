package org.endeavourhealth.core.queueing;

public class Route {
	public String regex;
	public String routeKey;

	public Route(String routeKey, String regex) {
		this.regex = regex;
		this.routeKey = routeKey;
	}
}
