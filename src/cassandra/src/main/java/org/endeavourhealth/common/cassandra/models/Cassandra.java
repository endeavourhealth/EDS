package org.endeavourhealth.common.cassandra.models;

import java.util.List;

public class Cassandra {
	private String username;
	private String password;
	private List<String> node;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getNode() {
		return node;
	}

	public void setNode(List<String> node) {
		this.node = node;
	}
}
