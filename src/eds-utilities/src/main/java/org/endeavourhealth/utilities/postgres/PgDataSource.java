package org.endeavourhealth.utilities.postgres;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class PgDataSource {
    public static DataSource get(String hostname, int port, String dbName, String username, String password) throws SQLException {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerName(hostname);
        pgSimpleDataSource.setPortNumber(port);
        pgSimpleDataSource.setDatabaseName(dbName);
        pgSimpleDataSource.setUser(username);
        pgSimpleDataSource.setPassword(password);
        return pgSimpleDataSource;
        //return DataSources.pooledDataSource(pgSimpleDataSource);
    }
}
