package org.endeavourhealth.patientexplorer.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.data.config.ConfigManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EnterpriseLiteDb {
	private static Connection _conn = null;
	public static Connection getConnection() throws SQLException, IOException {
		if (_conn == null) {
			JsonNode config = ConfigManager.getConfigurationAsJson("enterprise-lite");
			String url = config.get("url").asText();
			String username = config.get("username").asText();
			String password = config.get("password").asText();

			_conn = DriverManager.getConnection(url, username, password);
		}
		return _conn;
	}
}
