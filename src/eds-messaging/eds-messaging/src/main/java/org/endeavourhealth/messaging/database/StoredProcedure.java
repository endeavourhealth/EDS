package org.endeavourhealth.messaging.database;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StoredProcedure implements AutoCloseable
{
    private Connection connection;
    private String storedProcedureName;
    private Map<String, Object> parameters = new HashMap<>();
    private CallableStatement callableStatement;
    private ResultSet resultSet;

    public StoredProcedure(Connection connection, String storedProcedureName)
    {
        this.connection = connection;
        this.storedProcedureName = storedProcedureName;
    }

    public void setParameter(String parameterName, String value)
    {
        this.parameters.put(parameterName, value);
    }

    public void setParameter(String parameterName, int value)
    {
        this.parameters.put(parameterName, value);
    }

    public ResultSet executeQuery() throws SQLException
    {
        callableStatement = prepareStatement();

        resultSet = callableStatement.executeQuery();

        return resultSet;
    }

    public Object executeScalar() throws SQLException
    {
        ResultSet resultSet = executeQuery();

        if (resultSet.next())
            return resultSet.getObject(1);

        return null;
    }

    private CallableStatement prepareStatement() throws SQLException
    {
        String parametersDeclaration = String.join(", ", Collections.nCopies(parameters.size(), "?"));

        callableStatement = connection.prepareCall("call " + this.storedProcedureName + " (" + parametersDeclaration + ")");

        callableStatement.setEscapeProcessing(true);

        for (String parameterName : parameters.keySet())
            callableStatement.setObject(parameterName, parameters.get(parameterName));

        return callableStatement;
    }

    @Override
    public void close() throws SQLException
    {
        try
        {
            try
            {
                if (resultSet != null)
                    resultSet.close();
            }
            finally
            {
                if (callableStatement != null)
                    callableStatement.close();
            }
        }
        finally
        {
            if (connection != null)
                connection.close();
        }
    }
}
