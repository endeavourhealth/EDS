package org.endeavourhealth.utilities.postgres;

import org.endeavourhealth.utilities.configuration.model.DatabaseConnection;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class PgDataSource {
    public static DataSource get(String jdbcUrl, String username, String password) throws SQLException {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setUrl(jdbcUrl);
        pgSimpleDataSource.setUser(username);
        pgSimpleDataSource.setPassword(password);

        return pgSimpleDataSource;
    }

    public static DataSource get(DatabaseConnection databaseConnection) throws SQLException {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerName(databaseConnection.getHostname());
        pgSimpleDataSource.setPortNumber(databaseConnection.getPort().intValue());
        pgSimpleDataSource.setDatabaseName(databaseConnection.getDatabase());
        pgSimpleDataSource.setUser(databaseConnection.getUsername());
        pgSimpleDataSource.setPassword(databaseConnection.getPassword());

        return pgSimpleDataSource;
    }
}
