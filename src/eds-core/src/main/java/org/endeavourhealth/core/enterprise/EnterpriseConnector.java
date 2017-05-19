package org.endeavourhealth.core.enterprise;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;

public class EnterpriseConnector {

    public static Connection openConnection(String enterpriseConfigName) throws Exception {

        JsonNode config = ConfigManager.getConfigurationAsJson(enterpriseConfigName, "enterprise");

        String driverClass = config.get("driverClass").asText();
        String url = config.get("enterprise_url").asText();
        String username = config.get("enterprise_username").asText();
        String password = config.get("enterprise_password").asText();

        //force the driver to be loaded
        Class.forName(driverClass);

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        return conn;
    }
}
