package org.endeavourhealth.core.queueing;

public class RouteGroup {
	private String regex;
	private String routeKey;
	private String name;
	private String description;

	public RouteGroup() {}

	public RouteGroup(String name, String description, String routeKey, String regex) {
		this.name = name;
		this.description = description;
		this.regex = regex;
		this.routeKey = routeKey;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getRouteKey() {
		return routeKey;
	}

	public void setRouteKey(String routeKey) {
		this.routeKey = routeKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
