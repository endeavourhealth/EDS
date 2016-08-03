package org.endeavourhealth.sftpreader.utilities.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PgResultSet
{
    // make static for now
    public static Integer getInteger(ResultSet resultSet, String columnName) throws SQLException
    {
        int result = resultSet.getInt(columnName);

        if (resultSet.wasNull())
            return null;

        return result;
    }
}
