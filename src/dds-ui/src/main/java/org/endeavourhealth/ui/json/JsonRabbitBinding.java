package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonRabbitBinding {
	private String source;
	private String vhost;
	private String destination;
	private String destination_type;
	private String routing_key;
	private Object arguments;
	private String properties_key;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getRouting_key() {
		return routing_key;
	}

	public void setRouting_key(String routing_key) {
		this.routing_key = routing_key;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public String getDestination_type() {
		return destination_type;
	}

	public void setDestination_type(String destination_type) {
		this.destination_type = destination_type;
	}

	public Object getArguments() {
		return arguments;
	}

	public void setArguments(Object arguments) {
		this.arguments = arguments;
	}

	public String getProperties_key() {
		return properties_key;
	}

	public void setProperties_key(String properties_key) {
		this.properties_key = properties_key;
	}
}
