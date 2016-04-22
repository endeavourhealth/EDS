package org.endeavourhealth.messaging.database;

import java.sql.*;

public class DatabaseConnection
{
    public static final String DB_CONNECTION_STRING = "jdbc:postgresql://localhost/endeavour_resolution";
    public static final String USER_NAME = "";
    public static final String PASSWORD = "";

    public static Connection get() throws ClassNotFoundException, SQLException
    {
        // databaseName not used at present

        Class.forName(org.postgresql.Driver.class.getCanonicalName());
        return DriverManager.getConnection(DB_CONNECTION_STRING, USER_NAME, PASSWORD);
    }
}