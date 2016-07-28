package org.endeavourhealth.sftpreader.utilities.postgres;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PgStoredProc
{
    public interface IResultSetPopulator<T>
    {
        T populate(ResultSet resultSet) throws SQLException;
    }

    private DataSource dataSource;
    private String storedProcedureName;
    private Map<String, Object> parameters;
    private Map<String, Object> outParameters;

    public PgStoredProc(DataSource dataSource)
    {
        if (dataSource == null)
            throw new IllegalArgumentException("dataSource is null");

        this.dataSource = dataSource;
        this.parameters = new HashMap<>();
        this.outParameters = new HashMap<>();
    }

    public PgStoredProc setName(String storedProcedureName)
    {
        this.storedProcedureName = storedProcedureName;
        return this;
    }

    public <T> PgStoredProc addParameter(String name, T value)
    {
        this.parameters.put(name, value);
        return this;
    }

    public void execute() throws PgStoredProcException
    {
        List<HashMap<String, Object>> outParameters = executeQuery((resultSet) ->
        {
            HashMap<String, Object> hashMap = new HashMap<>();

            for (int i = 1; i < (resultSet.getMetaData().getColumnCount() + 1); i++)
                hashMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));

            return hashMap;
        });

        this.outParameters = outParameters.get(0);
    }

    public Object getOutParameter(String name)
    {
        return this.outParameters.get(name);
    }

    public <T extends Object> T executeSingleRow(IResultSetPopulator<T> rowMapper) throws PgStoredProcException
    {
        List<T> resultList = executeQuery(rowMapper);

        if (resultList == null)
            throw new PgStoredProcException("No results returned (null list)");

        if (resultList.size() == 0)
            throw new PgStoredProcException("No results returned");

        if (resultList.size() > 1)
            throw new PgStoredProcException("More than one result returned");

        return resultList.get(0);
    }

    public <T extends Object> List<T> executeQuery(IResultSetPopulator<T> rowMapper) throws PgStoredProcException
    {
        try
        {
            this.outParameters = new HashMap<>();

            try (Connection connection = this.dataSource.getConnection())
            {
                try (Statement statement = connection.createStatement())
                {
                    try (ResultSet resultSet = statement.executeQuery(getFormattedQuery()))
                    {
                        List<T> results = new ArrayList<>();

                        while (resultSet.next())
                            results.add(rowMapper.populate(resultSet));

                        return results;
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new PgStoredProcException("executeQuery error, see inner exception", e);
        }
    }

    private String getFormattedQuery()
    {
        if (StringUtils.isEmpty(storedProcedureName))
            throw new IllegalArgumentException("storedProcedureName is empty");

        return "select * from " + storedProcedureName
                + "("
                + getFormattedParameters()
                + ");";
    }

    private String getFormattedParameters()
    {
        return StringUtils.join(parameters
                .keySet()
                .stream()
                .map(t -> t + " := " + getFormattedParameterValue(parameters.get(t).toString()))
                .toArray(), ',');
    }

    private static String getFormattedParameterValue(Object value)
    {
        if ((value instanceof Integer) || (value instanceof Long))
            return value.toString();
        else if ((value instanceof Character) || (value instanceof String))
            return "'" + value + "'";
        else if (value instanceof java.time.LocalDate)
            return "'" + ((java.time.LocalDate)value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'";

        throw new NotImplementedException("Parameter type not supported");
    }
}
