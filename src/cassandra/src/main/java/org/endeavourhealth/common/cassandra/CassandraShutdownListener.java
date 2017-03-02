package org.endeavourhealth.common.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public final class CassandraShutdownListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraShutdownListener.class);

    public void contextInitialized(ServletContextEvent contextEvent) {
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {

        CassandraConnector.getInstance().close();

        // also shutdown postgres driver
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOG.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                LOG.error(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}
